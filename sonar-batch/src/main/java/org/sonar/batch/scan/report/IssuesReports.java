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
package org.sonar.batch.scan.report;

import org.sonar.batch.scan.ProjectAnalysisMode;

import org.sonar.api.batch.BatchSide;

@BatchSide
public class IssuesReports {

  private final ProjectAnalysisMode analysisMode;
  private final Reporter[] reporters;

  public IssuesReports(ProjectAnalysisMode analysisMode, Reporter... reporters) {
    this.reporters = reporters;
    this.analysisMode = analysisMode;
  }

  public void execute() {
    if (analysisMode.isPreview() || analysisMode.isMediumTest()) {
      for (Reporter reporter : reporters) {
        reporter.execute();
      }
    }
  }
}
