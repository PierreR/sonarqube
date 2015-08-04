/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.computation.step;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.MeasureComputerImplementation;
import org.sonar.api.ce.measure.MeasureComputerProvider;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;
import org.sonar.api.utils.dag.DirectAcyclicGraph;
import org.sonar.server.computation.component.DepthTraversalTypeAwareVisitor;
import org.sonar.server.computation.component.ProjectSettingsRepository;
import org.sonar.server.computation.component.TreeRootHolder;
import org.sonar.server.computation.measure.MeasureRepository;
import org.sonar.server.computation.measure.api.MeasureComputerImplementationContext;
import org.sonar.server.computation.measure.api.MeasureComputerProviderContext;
import org.sonar.server.computation.metric.MetricRepository;

import static org.sonar.server.computation.component.Component.Type.FILE;
import static org.sonar.server.computation.component.ComponentVisitor.Order.PRE_ORDER;

public class ComputePluginMeasuresStep implements ComputationStep {

  private final TreeRootHolder treeRootHolder;
  private final MetricRepository metricRepository;
  private final MeasureRepository measureRepository;

  /**
   * TODO create a MeasureComputerHolder that will have the responsability to load measurecomputer, sort them, check them, etc...
   */
  private final Metrics[] metricsRepositories;
  private final MeasureComputerProvider[] measureComputerProviders;
  private final ProjectSettingsRepository settings;

  public ComputePluginMeasuresStep(TreeRootHolder treeRootHolder, MetricRepository metricRepository, MeasureRepository measureRepository, ProjectSettingsRepository settings,
                                   Metrics[] metricsRepositories, MeasureComputerProvider[] measureComputerProviders) {
    this.treeRootHolder = treeRootHolder;
    this.metricRepository = metricRepository;
    this.measureRepository = measureRepository;
    this.metricsRepositories = metricsRepositories;
    this.measureComputerProviders = measureComputerProviders;
    this.settings = settings;
  }

  @Override
  public void execute() {
    new NewMetricDefinitionsVisitor().visit(treeRootHolder.getRoot());
  }

  private class NewMetricDefinitionsVisitor extends DepthTraversalTypeAwareVisitor {

    private final Iterable<MeasureComputer> measureComputers;

    public NewMetricDefinitionsVisitor() {
      super(FILE, PRE_ORDER);
      MeasureComputerProviderContext context = new MeasureComputerProviderContext();
      for (MeasureComputerProvider provider : measureComputerProviders) {
        provider.register(context);
      }
      this.measureComputers = sortComputers(context.getMeasureComputers());
    }

    @Override
    public void visitAny(org.sonar.server.computation.component.Component component) {
      for (MeasureComputer computer : measureComputers) {
        MeasureComputerImplementationContext measureComputerContext = new MeasureComputerImplementationContext(component, computer, settings, measureRepository, metricRepository);
        computer.getImplementation().compute(measureComputerContext);
      }
    }
  }

  private static Iterable<MeasureComputer> sortComputers(List<MeasureComputer> computers) {
    List<MeasureComputer> allComputers = new ArrayList<>(computers);
    allComputers.add(CoreMeasureComputer.INSTANCE);

    Map<String, MeasureComputer> computersByOutputMetric = new HashMap<>();
    Map<String, MeasureComputer> computersByInputMetric = new HashMap<>();
    for (MeasureComputer computer : allComputers) {
      for (String outputMetric : computer.getOutputMetrics()) {
        computersByOutputMetric.put(outputMetric, computer);
      }
      for (String inputMetric : computer.getInputMetrics()) {
        computersByInputMetric.put(inputMetric, computer);
      }
    }

    DirectAcyclicGraph dag = new DirectAcyclicGraph();
    for (MeasureComputer computer : allComputers) {
      dag.add(computer);
      for (MeasureComputer dependency : getDependencies(computer, computersByOutputMetric)) {
        dag.add(computer, dependency);
      }
      for (MeasureComputer generates : getDependents(computer, computersByInputMetric)) {
        dag.add(generates, computer);
      }
    }
    return FluentIterable.from(dag.sort()).filter(Predicates.in(computers));
  }

  private static Iterable<MeasureComputer> getDependencies(MeasureComputer measureComputer, Map<String, MeasureComputer> computersByOutputMetric) {
    return FluentIterable.from(measureComputer.getInputMetrics()).transform(new ToComputerByKey(computersByOutputMetric));
  }

  private static Iterable<MeasureComputer> getDependents(MeasureComputer measureComputer, Map<String, MeasureComputer> computersByInputMetric) {
    return FluentIterable.from(measureComputer.getInputMetrics()).transform(new ToComputerByKey(computersByInputMetric));
  }

  private static class ToComputerByKey implements Function<String, MeasureComputer> {
    private final Map<String, MeasureComputer> computersByMetric;

    private ToComputerByKey(Map<String, MeasureComputer> computersByMetric) {
      this.computersByMetric = computersByMetric;
    }

    @Nullable
    @Override
    public MeasureComputer apply(@Nonnull String metricKey) {
      return computersByMetric.get(metricKey);
    }
  }

  private enum CoreMeasureComputer implements MeasureComputer {
    INSTANCE;

    private static final Set<String> CORE_METRIC_KEYS = FluentIterable.from(CoreMetrics.getMetrics()).transform(MetricToKey.INSTANCE).toSet();

    @Override
    public Set<String> getInputMetrics() {
      return Collections.emptySet();
    }

    @Override
    public Set<String> getOutputMetrics() {
      return CORE_METRIC_KEYS;
    }

    @Override
    public MeasureComputerImplementation getImplementation() {
      throw new UnsupportedOperationException("No implementation on core metrics");
    }
  }

  private enum MetricToKey implements Function<Metric, String> {
    INSTANCE;

    @Nullable
    @Override
    public String apply(@Nonnull Metric input) {
      return input.key();
    }
  }

  @Override
  public String getDescription() {
    return "Compute measures from plugin";
  }

}
