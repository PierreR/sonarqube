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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.MeasureComputerProvider;

public class MeasureComputerProviderContext implements MeasureComputerProvider.Context {

  private final List<MeasureComputer> measureComputers = new ArrayList<>();
  private final Map<String, MeasureComputer> computerByOutputMetrics = new HashMap<>();
  private final Set<String> allOutputMetrics = new HashSet<>();

  @Override
  public MeasureComputerProvider.Context add(MeasureComputer measureComputer) {
    checkOutputMetricsNotAlreadyDefinedByAnotherComputer(measureComputer);
    this.allOutputMetrics.addAll(measureComputer.getOutputMetrics());
    this.measureComputers.add(measureComputer);
    for (String metric : measureComputer.getOutputMetrics()) {
      computerByOutputMetrics.put(metric, measureComputer);
    }
    return this;
  }

  public List<MeasureComputer> getMeasureComputers() {
    return measureComputers;
  }

  private void checkOutputMetricsNotAlreadyDefinedByAnotherComputer(MeasureComputer measureComputer) {
    for (String metric : measureComputer.getOutputMetrics()) {
      if (this.allOutputMetrics.contains(metric)) {
        MeasureComputer otherComputer = computerByOutputMetrics.get(metric);
        throw new UnsupportedOperationException(String.format(
          "The output metric '%s' is already declared by another computer. This computer has these input metrics '%s' and these output metrics '%s'",
          metric, otherComputer.getInputMetrics(), otherComputer.getOutputMetrics()));
      }
    }
  }

  @Override
  public MeasureComputer.MeasureComputerBuilder newMeasureComputerBuilder() {
    return new MeasureComputerImpl.MeasureComputerBuilderImpl();
  }
}
