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

import org.sonar.api.ce.measure.Measure;

public class MeasureImpl implements Measure {

  private final org.sonar.server.computation.measure.Measure measure;

  public MeasureImpl(org.sonar.server.computation.measure.Measure measure) {
    this.measure = measure;
  }

  @Override
  public int getIntValue() {
    return measure.getIntValue();
  }

  @Override
  public long getLongValue() {
    return measure.getIntValue();
  }

  @Override
  public double getDoubleValue() {
    return measure.getDoubleValue();
  }

  @Override
  public String getStringValue() {
    return measure.getStringValue();
  }
}
