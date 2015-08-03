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

package org.sonar.api.ce.measure;

import java.util.Set;

/**
 * This class is used to define which metrics are required to compute some measures on some given metrics, and to define the implementation of the measures computation
 */
public interface MeasureComputer {

  /**
   * Return the metric keys that can be read using {@link org.sonar.api.ce.measure.MeasureComputerImplementation.Context}.
   */
  Set<String> getInputMetrics();

  /**
   * Return the metric keys that can be create using {@link org.sonar.api.ce.measure.MeasureComputerImplementation.Context}.
   */
  Set<String> getOutputMetrics();

  MeasureComputerImplementation getImplementation();

  interface MeasureComputerBuilder {

    MeasureComputerBuilder setInputMetrics(String... inputMetrics);

    MeasureComputerBuilder setOutputMetrics(String... outMetrics);

    MeasureComputerBuilder setImplementation(MeasureComputerImplementation impl);

    /**
     * @throws IllegalStateException if there's not at least one input metrics
     * @throws IllegalStateException if there's not at least one output metrics
     * @throws IllegalStateException if there's no implementation
     */
    MeasureComputer build();

  }
}
