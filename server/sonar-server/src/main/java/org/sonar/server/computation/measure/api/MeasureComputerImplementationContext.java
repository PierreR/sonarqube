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

package org.sonar.server.computation.measure.api;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.MeasureComputerImplementation;
import org.sonar.api.ce.measure.Settings;
import org.sonar.server.computation.component.ProjectSettingsRepository;
import org.sonar.server.computation.measure.MeasureRepository;
import org.sonar.server.computation.metric.Metric;
import org.sonar.server.computation.metric.MetricRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static org.sonar.server.computation.measure.Measure.newMeasureBuilder;

public class MeasureComputerImplementationContext implements MeasureComputerImplementation.Context {

  private final org.sonar.server.computation.component.Component component;
  private final MeasureComputer measureComputer;
  private final ProjectSettingsRepository settings;
  private final MeasureRepository measureRepository;
  private final MetricRepository metricRepository;

  public MeasureComputerImplementationContext(org.sonar.server.computation.component.Component component, MeasureComputer measureComputer,
    ProjectSettingsRepository settings, MeasureRepository measureRepository, MetricRepository metricRepository) {
    this.measureComputer = measureComputer;
    this.settings = settings;
    this.component = component;
    this.measureRepository = measureRepository;
    this.metricRepository = metricRepository;
  }

  @Override
  public Component getComponent() {
    return new ComponentImpl(component);
  }

  @Override
  public Settings getSettings() {
    return new Settings() {
      @Override
      public String getString(String key) {
        return settings.getProjectSettings(component.getKey()).getString(key);
      }
    };
  }

  @Override
  @CheckForNull
  public Measure getMeasure(String metric) {
    validateInputMetric(metric);
    Optional<org.sonar.server.computation.measure.Measure> measure = measureRepository.getRawMeasure(component, metricRepository.getByKey(metric));
    if (measure.isPresent()) {
      return new MeasureImpl(measure.get());
    }
    return null;
  }

  @Override
  public Iterable<Measure> getChildrenMeasures(String metric) {
    validateInputMetric(metric);
    return FluentIterable.from(component.getChildren())
      .transform(new ComponentToMeasure(metricRepository.getByKey(metric)))
      .transform(ToMeasureAPI.INSTANCE)
      .filter(Predicates.notNull());
  }

  @Override
  public void addMeasure(String metricKey, int value) {
    Metric metric = metricRepository.getByKey(metricKey);
    validateAddMeasure(metric);
    measureRepository.add(component, metric, newMeasureBuilder().create(value));
  }

  @Override
  public void addMeasure(String metricKey, double value) {
    Metric metric = metricRepository.getByKey(metricKey);
    validateAddMeasure(metric);
    measureRepository.add(component, metric, newMeasureBuilder().create(value));
  }

  @Override
  public void addMeasure(String metricKey, long value) {
    Metric metric = metricRepository.getByKey(metricKey);
    validateAddMeasure(metric);
    measureRepository.add(component, metric, newMeasureBuilder().create(value));
  }

  @Override
  public void addMeasure(String metricKey, String value) {
    Metric metric = metricRepository.getByKey(metricKey);
    validateAddMeasure(metric);
    measureRepository.add(component, metric, newMeasureBuilder().create(value));
  }

  private void validateInputMetric(String metric) {
    Set<String> allowedMetrics = new HashSet<>();
    allowedMetrics.addAll(measureComputer.getInputMetrics());
    allowedMetrics.addAll(measureComputer.getOutputMetrics());
    checkArgument(allowedMetrics.contains(metric), "Only metrics in %s can be used to load measures", measureComputer.getInputMetrics());
  }

  private void validateAddMeasure(Metric metric) {
    checkArgument(measureComputer.getOutputMetrics().contains(metric.getKey()), "Only metrics in %s can be used to add measures", measureComputer.getOutputMetrics());
    if (measureRepository.getRawMeasure(component, metric).isPresent()) {
      throw new UnsupportedOperationException(String.format("A measure on metric '%s' already exists on component '%s'", metric.getKey(), component.getKey()));
    }
  }

  private class ComponentToMeasure implements Function<org.sonar.server.computation.component.Component, Optional<org.sonar.server.computation.measure.Measure>> {

    private final Metric metric;

    public ComponentToMeasure(Metric metric) {
      this.metric = metric;
    }

    @Nullable
    @Override
    public Optional<org.sonar.server.computation.measure.Measure> apply(@Nonnull org.sonar.server.computation.component.Component input) {
      return measureRepository.getRawMeasure(input, metric);
    }
  }

  private enum ToMeasureAPI implements Function<Optional<org.sonar.server.computation.measure.Measure>, Measure> {
    INSTANCE;

    @Nullable
    @Override
    public Measure apply(@Nonnull Optional<org.sonar.server.computation.measure.Measure> input) {
      return input.isPresent() ? new MeasureImpl(input.get()) : null;
    }
  }
}
