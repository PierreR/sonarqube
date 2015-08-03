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

import javax.annotation.CheckForNull;

/**
 * This interface must be instantiate to define how the measures are computed.
 */
public interface MeasureComputerImplementation {

  /**
   * This method will be called on each component of the projects.
   */
  void compute(Context ctx);

  interface Context {

    /**
     * Return the current component.
     */
    Component getComponent();

    /**
     * Return settings of the current component.
     */
    Settings getSettings();

    /**
     * Return the measure from a given metric on the current component.
     *
     * @throws IllegalArgumentException if the metric is not list in {@link MeasureComputer#getInputMetrics()}
     */
    @CheckForNull
    Measure getMeasure(String metric);

    /**
     * Return measures from a given metric on children of the current component.
     *
     * @throws IllegalArgumentException if the metric is not list in {@link MeasureComputer#getInputMetrics()} or in {@link MeasureComputer#getOutputMetrics()}
     */
    Iterable<Measure> getChildrenMeasures(String metric);

    /**
     * Add a new measure of a given metric.
     *
     * @throws IllegalArgumentException if the metric is not list in {@link MeasureComputer#getOutputMetrics()}
     * @throws UnsupportedOperationException when trying to add a measure when one already exists for the specified Component/Metric paar
     */
    void addMeasure(String metric, int value);

    /**
     * Add a new measure of a given metric.
     *
     * @throws IllegalArgumentException if the metric is not list in {@link MeasureComputer#getOutputMetrics()}
     * @throws UnsupportedOperationException when trying to add a measure when one already exists for the specified Component/Metric paar
     */
    void addMeasure(String metric, double value);

    /**
     * Add a new measure of a given metric.
     *
     * @throws IllegalArgumentException if the metric is not list in {@link MeasureComputer#getOutputMetrics()}
     * @throws UnsupportedOperationException when trying to add a measure when one already exists for the specified Component/Metric paar
     */
    void addMeasure(String metric, long value);

    /**
     * Add a new measure of a given metric.
     *
     * @throws IllegalArgumentException if the metric is not list in {@link MeasureComputer#getOutputMetrics()}
     * @throws UnsupportedOperationException when trying to add a measure when one already exists for the specified Component/Metric paar
     */
    void addMeasure(String metric, String value);

  }

}
