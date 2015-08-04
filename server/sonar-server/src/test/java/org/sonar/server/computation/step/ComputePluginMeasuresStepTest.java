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

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputerImplementation;
import org.sonar.api.ce.measure.MeasureComputerProvider;
import org.sonar.api.measures.Metrics;
import org.sonar.server.computation.batch.TreeRootHolderRule;
import org.sonar.server.computation.measure.MeasureRepositoryRule;
import org.sonar.server.computation.metric.MetricRepositoryRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.api.measures.CoreMetrics.COMMENT_LINES;
import static org.sonar.api.measures.CoreMetrics.COMMENT_LINES_KEY;
import static org.sonar.api.measures.CoreMetrics.NCLOC;
import static org.sonar.api.measures.CoreMetrics.NCLOC_KEY;
import static org.sonar.server.computation.component.Component.Type.DIRECTORY;
import static org.sonar.server.computation.component.Component.Type.FILE;
import static org.sonar.server.computation.component.Component.Type.MODULE;
import static org.sonar.server.computation.component.Component.Type.PROJECT;
import static org.sonar.server.computation.component.DumbComponent.builder;
import static org.sonar.server.computation.measure.Measure.newMeasureBuilder;
import static org.sonar.server.computation.measure.MeasureRepoEntry.entryOf;
import static org.sonar.server.computation.measure.MeasureRepoEntry.toEntries;

public class ComputePluginMeasuresStepTest {

  private static final String NEW_METRIC_KEY = "new_metric_key";
  private static final String NEW_METRIC_NAME = "new metric name";

  private static final int ROOT_REF = 1;
  private static final int MODULE_REF = 12;
  private static final int DIRECTORY_REF = 123;
  private static final int FILE_1_REF = 1231;
  private static final int FILE_2_REF = 1232;

  @Rule
  public TreeRootHolderRule treeRootHolder = new TreeRootHolderRule();

  @Rule
  public MetricRepositoryRule metricRepository = new MetricRepositoryRule()
    .add(NCLOC)
    .add(COMMENT_LINES)
    .add(NEW_METRIC);

  @Rule
  public MeasureRepositoryRule measureRepository = MeasureRepositoryRule.create(treeRootHolder, metricRepository);

  @Before
  public void setUp() throws Exception {
    treeRootHolder.setRoot(
      builder(PROJECT, ROOT_REF)
        .addChildren(
          builder(MODULE, MODULE_REF)
            .addChildren(
              builder(DIRECTORY, DIRECTORY_REF)
                .addChildren(
                  builder(FILE, FILE_1_REF).build(),
                  builder(FILE, FILE_2_REF).build()
                ).build()
            ).build()
        ).build());
  }

  @Test
  public void compute_plugin_measure() throws Exception {
    measureRepository.addRawMeasure(FILE_1_REF, NCLOC_KEY, newMeasureBuilder().create(10));
    measureRepository.addRawMeasure(FILE_1_REF, COMMENT_LINES_KEY, newMeasureBuilder().create(2));
    measureRepository.addRawMeasure(FILE_2_REF, NCLOC_KEY, newMeasureBuilder().create(40));
    measureRepository.addRawMeasure(FILE_2_REF, COMMENT_LINES_KEY, newMeasureBuilder().create(5));
    measureRepository.addRawMeasure(DIRECTORY_REF, NCLOC_KEY, newMeasureBuilder().create(50));
    measureRepository.addRawMeasure(DIRECTORY_REF, COMMENT_LINES_KEY, newMeasureBuilder().create(7));
    measureRepository.addRawMeasure(MODULE_REF, NCLOC_KEY, newMeasureBuilder().create(50));
    measureRepository.addRawMeasure(MODULE_REF, COMMENT_LINES_KEY, newMeasureBuilder().create(7));
    measureRepository.addRawMeasure(ROOT_REF, NCLOC_KEY, newMeasureBuilder().create(50));
    measureRepository.addRawMeasure(ROOT_REF, COMMENT_LINES_KEY, newMeasureBuilder().create(7));

    MeasureComputerProvider[] providers = new MeasureComputerProvider[] {new NewMeasureComputerProvider(
      array(NCLOC_KEY, COMMENT_LINES_KEY),
      array(NEW_METRIC_KEY),
      new MeasureComputerImplementation() {
        @Override
        public void compute(Context ctx) {
          Measure ncloc = ctx.getMeasure(NCLOC_KEY);
          Measure comment = ctx.getMeasure(COMMENT_LINES_KEY);
          if (ncloc != null && comment != null) {
            ctx.addMeasure(NEW_METRIC_KEY, ncloc.getIntValue() + comment.getIntValue());
          }
        }
      }
      )};
    ComputationStep underTest = new ComputePluginMeasuresStep(treeRootHolder, metricRepository, measureRepository, null, array(new TestMetrics()), providers);
    underTest.execute();

    assertThat(toEntries(measureRepository.getAddedRawMeasures(FILE_1_REF))).containsOnly(entryOf(NEW_METRIC_KEY, newMeasureBuilder().create(12)));
    assertThat(toEntries(measureRepository.getAddedRawMeasures(FILE_2_REF))).containsOnly(entryOf(NEW_METRIC_KEY, newMeasureBuilder().create(45)));
    assertThat(toEntries(measureRepository.getAddedRawMeasures(DIRECTORY_REF))).containsOnly(entryOf(NEW_METRIC_KEY, newMeasureBuilder().create(57)));
    assertThat(toEntries(measureRepository.getAddedRawMeasures(MODULE_REF))).containsOnly(entryOf(NEW_METRIC_KEY, newMeasureBuilder().create(57)));
    assertThat(toEntries(measureRepository.getAddedRawMeasures(ROOT_REF))).containsOnly(entryOf(NEW_METRIC_KEY, newMeasureBuilder().create(57)));
  }

  @Test
  public void sort_computers() throws Exception {
    MeasureComputerImplementation implementation1 = mock(MeasureComputerImplementation.class);
    MeasureComputerImplementation implementation2 = mock(MeasureComputerImplementation.class);
    MeasureComputerImplementation implementation3 = mock(MeasureComputerImplementation.class);

    MeasureComputerProvider[] providers = new MeasureComputerProvider[] {
      // Should be the last to be executed
      new NewMeasureComputerProvider(
        array("metric3"),
        array("metric4"),
        implementation3),
      // Should be the first to be executed
      new NewMeasureComputerProvider(
        array(NCLOC_KEY),
        array("metric2"),
        implementation1),
      // Should be the second to be executed
      new NewMeasureComputerProvider(
        array("metric2"),
        array("metric3"),
        implementation2)
    };
    ComputationStep underTest = new ComputePluginMeasuresStep(treeRootHolder, metricRepository, measureRepository, null, array(new TestMetrics()), providers);
    underTest.execute();

    InOrder inOrder = inOrder(implementation1, implementation2, implementation3);
    inOrder.verify(implementation1).compute(any(MeasureComputerImplementation.Context.class));
    inOrder.verify(implementation2).compute(any(MeasureComputerImplementation.Context.class));
    inOrder.verify(implementation3).compute(any(MeasureComputerImplementation.Context.class));
  }

  @Test
  public void not_fail_if_input_metrics_are_same_as_output_metrics() throws Exception {
    MeasureComputerImplementation implementation = mock(MeasureComputerImplementation.class);
    MeasureComputerProvider[] providers = new MeasureComputerProvider[] {
      new NewMeasureComputerProvider(
        array(NEW_METRIC_KEY),
        array(NEW_METRIC_KEY),
        implementation),
    };
    ComputationStep underTest = new ComputePluginMeasuresStep(treeRootHolder, metricRepository, measureRepository, null, array(new TestMetrics()), providers);
    underTest.execute();

    verify(implementation, times(5)).compute(any(MeasureComputerImplementation.Context.class));
  }

  @Test
  public void nothing_to_do_when_no_plugin_computers() throws Exception {
    ComputationStep underTest = new ComputePluginMeasuresStep(treeRootHolder, metricRepository, measureRepository, null, array(new TestMetrics()), new MeasureComputerProvider[] {});
    underTest.execute();

    assertThat(measureRepository.getAddedRawMeasures(FILE_1_REF)).isEmpty();
    assertThat(measureRepository.getAddedRawMeasures(FILE_2_REF)).isEmpty();
    assertThat(measureRepository.getAddedRawMeasures(DIRECTORY_REF)).isEmpty();
    assertThat(measureRepository.getAddedRawMeasures(MODULE_REF)).isEmpty();
    assertThat(measureRepository.getAddedRawMeasures(ROOT_REF)).isEmpty();
  }

  @Test
  @Ignore
  public void fail_if_input_metrics_are_not_generated() throws Exception {
    MeasureComputerProvider[] providers = new MeasureComputerProvider[] {
      new NewMeasureComputerProvider(
        array("unknown"),
        array(NEW_METRIC_KEY),
        mock(MeasureComputerImplementation.class)),
    };
    ComputationStep underTest = new ComputePluginMeasuresStep(treeRootHolder, metricRepository, measureRepository, null, array(new TestMetrics()), providers);
    underTest.execute();
  }

  private static class NewMeasureComputerProvider implements MeasureComputerProvider {

    private final String[] inputMetrics;
    private final String[] outputMetrics;
    private final MeasureComputerImplementation measureComputerImplementation;

    public NewMeasureComputerProvider(String[] inputMetrics, String[] outputMetrics, MeasureComputerImplementation measureComputerImplementation) {
      this.inputMetrics = inputMetrics;
      this.outputMetrics = outputMetrics;
      this.measureComputerImplementation = measureComputerImplementation;
    }

    @Override
    public void register(Context ctx) {
      ctx.add(ctx.newMeasureComputerBuilder()
        .setInputMetrics(inputMetrics)
        .setOutputMetrics(outputMetrics)
        .setImplementation(measureComputerImplementation)
        .build());
    }
  }

  public static final org.sonar.api.measures.Metric<Integer> NEW_METRIC = new org.sonar.api.measures.Metric.Builder(NEW_METRIC_KEY, NEW_METRIC_NAME,
    org.sonar.api.measures.Metric.ValueType.INT)
    .create();

  private class TestMetrics implements Metrics {
    @Override
    public List<org.sonar.api.measures.Metric> getMetrics() {
      return Lists.<org.sonar.api.measures.Metric>newArrayList(NEW_METRIC);
    }
  }

}
