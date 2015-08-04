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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.MeasureComputerImplementation;

import static com.google.common.base.Preconditions.checkArgument;

public class MeasureComputerImpl implements MeasureComputer {

  private final Set<String> inputMetricKeys;
  private final Set<String> outputMetrics;
  private final MeasureComputerImplementation measureComputerImplementation;

  public MeasureComputerImpl(MeasureComputerBuilderImpl builder) {
    this.inputMetricKeys = ImmutableSet.copyOf(builder.inputMetricKeys);
    this.outputMetrics = ImmutableSet.copyOf(builder.outputMetrics);
    this.measureComputerImplementation = builder.measureComputerImplementation;
  }

  @Override
  public Set<String> getInputMetrics() {
    return inputMetricKeys;
  }

  @Override
  public Set<String> getOutputMetrics() {
    return outputMetrics;
  }

  @Override
  public MeasureComputerImplementation getImplementation() {
    return measureComputerImplementation;
  }

  @Override
  public String toString() {
    return "MeasureComputerImpl{" +
      "inputMetricKeys=" + inputMetricKeys +
      ", outputMetrics=" + outputMetrics +
      '}';
  }

  public static class MeasureComputerBuilderImpl implements MeasureComputerBuilder {

    private String[] inputMetricKeys;
    private String[] outputMetrics;
    private MeasureComputerImplementation measureComputerImplementation;

    @Override
    public MeasureComputerBuilder setInputMetrics(String... inputMetrics) {
      this.inputMetricKeys = inputMetrics;
      return this;
    }

    @Override
    public MeasureComputerBuilder setOutputMetrics(String... outputMetrics) {
      this.outputMetrics = outputMetrics;
      return this;
    }

    @Override
    public MeasureComputerBuilder setImplementation(MeasureComputerImplementation impl) {
      this.measureComputerImplementation = impl;
      return this;
    }

    @Override
    public MeasureComputer build() {
      checkArgument(this.inputMetricKeys != null && inputMetricKeys.length > 0, "At least one input metrics must be defined");
      checkArgument(this.outputMetrics != null && outputMetrics.length > 0, "At least one output metrics must be defined");
      checkArgument(this.measureComputerImplementation != null, "The implementation is missing");
      return new MeasureComputerImpl(this);
    }
  }
}
