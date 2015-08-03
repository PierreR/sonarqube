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

public class MeasureComputerProviderContextTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  MeasureComputerProviderContext underTest = new MeasureComputerProviderContext();

  @Test
  public void return_empty_list() throws Exception {
    assertThat(underTest.getMeasureComputers()).isEmpty();
  }

  @Test
  public void add_measure_computer() throws Exception {
    underTest.add(newMeasureComputer("debt_density"));

    assertThat(underTest.getMeasureComputers()).hasSize(1);
  }

  @Test
  public void fail_with_unsupported_operation_exception_when_output_metrics_have_already_been_registered() throws Exception {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("The output metric 'debt_density' is already declared by another computer. This computer has these input metrics '[ncloc, debt]' and these output metrics '[debt_by_line, debt_density]");

    underTest.add(newMeasureComputer("debt_by_line","debt_density"));
    underTest.add(newMeasureComputer("total_debt", "debt_density"));
  }

  private MeasureComputer newMeasureComputer(String... outputMetrics) {
    return new MeasureComputerImpl.MeasureComputerBuilderImpl()
      .setInputMetrics("ncloc", "debt")
      .setOutputMetrics(outputMetrics)
      .setImplementation(new MeasureComputerImplementation() {
        @Override
        public void compute(Context ctx) {
          // Nothing to do here for this test
        }
      })
      .build();
  }

}
