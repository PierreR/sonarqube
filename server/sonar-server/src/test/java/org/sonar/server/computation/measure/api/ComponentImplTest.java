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
import org.sonar.api.ce.measure.Component;
import org.sonar.server.computation.component.DumbComponent;
import org.sonar.server.computation.component.FileAttributes;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentImplTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void create_project() throws Exception {
    ComponentImpl component = new ComponentImpl(DumbComponent.DUMB_PROJECT);

    assertThat(component.getType()).isEqualTo(Component.Type.PROJECT);
  }

  @Test
  public void create_source_file() throws Exception {
    ComponentImpl component = new ComponentImpl(
      DumbComponent.builder(org.sonar.server.computation.component.Component.Type.FILE, 1)
        .setFileAttributes(new FileAttributes(false, "xoo"))
        .build()
      );

    assertThat(component.getType()).isEqualTo(Component.Type.FILE);
    assertThat(component.getFileAttributes().getLanguageKey()).isEqualTo("xoo");
    assertThat(component.getFileAttributes().isUnitTest()).isFalse();
  }

  @Test
  public void create_test_file() throws Exception {
    ComponentImpl component = new ComponentImpl(
      DumbComponent.builder(org.sonar.server.computation.component.Component.Type.FILE, 1)
        .setFileAttributes(new FileAttributes(true, null))
        .build()
      );

    assertThat(component.getType()).isEqualTo(Component.Type.FILE);
    assertThat(component.getFileAttributes().isUnitTest()).isTrue();
    assertThat(component.getFileAttributes().getLanguageKey()).isNull();
  }

  @Test
  public void fail_with_ISE_when_calling_get_file_attributes_on_not_file() throws Exception {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Only component of type FILE have a FileAttributes object");

    ComponentImpl component = new ComponentImpl(DumbComponent.DUMB_PROJECT);
    component.getFileAttributes();
  }

}
