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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.MeasureComputerImplementation;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasureComputerImplTest {

  private static final MeasureComputerImplementation DEFAULT_MEASURE_COMPUTER_IMPLEMENTATION = new MeasureComputerImplementation() {
    @Override
    public void compute(MeasureComputerImplementation.Context ctx) {
      // Nothing here for this test
    }
  };

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void build_measure_computer() throws Exception {
    MeasureComputer measureComputer = new MeasureComputerImpl.MeasureComputerBuilderImpl()
      .setInputMetrics("ncloc", "comment")
      .setOutputMetrics("comment_density_1", "comment_density_2")
      .setImplementation(DEFAULT_MEASURE_COMPUTER_IMPLEMENTATION)
      .build();

    assertThat(measureComputer.getInputMetrics()).containsOnly("ncloc", "comment");
    assertThat(measureComputer.getOutputMetrics()).containsOnly("comment_density_1", "comment_density_2");
    assertThat(measureComputer.getImplementation()).isEqualTo(DEFAULT_MEASURE_COMPUTER_IMPLEMENTATION);
  }

  @Test
  public void fail_with_IAE_when_no_input_metrics() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("At least one input metrics must be defined");

    new MeasureComputerImpl.MeasureComputerBuilderImpl()
      .setOutputMetrics("comment_density_1", "comment_density_2")
      .setImplementation(DEFAULT_MEASURE_COMPUTER_IMPLEMENTATION)
      .build();
  }

  @Test
  public void fail_with_IAE_with_empty_input_metrics() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("At least one input metrics must be defined");

    new MeasureComputerImpl.MeasureComputerBuilderImpl()
      .setInputMetrics()
      .setOutputMetrics("comment_density_1", "comment_density_2")
      .setImplementation(DEFAULT_MEASURE_COMPUTER_IMPLEMENTATION)
      .build();
  }

  @Test
  public void fail_with_IAE_when_no_output_metrics() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("At least one output metrics must be defined");

    new MeasureComputerImpl.MeasureComputerBuilderImpl()
      .setInputMetrics("ncloc", "comment")
      .setImplementation(DEFAULT_MEASURE_COMPUTER_IMPLEMENTATION)
      .build();
  }

  @Test
  public void fail_with_IAE_with_empty_output_metrics() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("At least one output metrics must be defined");

    new MeasureComputerImpl.MeasureComputerBuilderImpl()
      .setInputMetrics("ncloc", "comment")
      .setOutputMetrics()
      .setImplementation(DEFAULT_MEASURE_COMPUTER_IMPLEMENTATION)
      .build();
  }

  @Test
  public void fail_with_IAE_when_no_implementation() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("The implementation is missing");

    new MeasureComputerImpl.MeasureComputerBuilderImpl()
      .setInputMetrics("ncloc", "comment")
      .setOutputMetrics("comment_density_1", "comment_density_2")
      .build();
  }

}
