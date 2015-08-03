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

import javax.annotation.CheckForNull;
import org.sonar.api.ce.measure.Component;

import static com.google.common.base.Preconditions.checkState;

public class ComponentImpl implements Component {

  private final Type type;
  private final FileAttributes fileAttributes;

  public ComponentImpl(org.sonar.server.computation.component.Component component) {
    this.type = Type.valueOf(component.getType().name());
    this.fileAttributes = createFileAttributes(component);
  }

  @CheckForNull
  private static FileAttributes createFileAttributes(org.sonar.server.computation.component.Component component) {
    if (component.getType() != org.sonar.server.computation.component.ComponentImpl.Type.FILE) {
      return null;
    }

    return new FileAttributesImpl(
      component.getFileAttributes().getLanguageKey(),
      component.getFileAttributes().isUnitTest());
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public FileAttributes getFileAttributes() {
    checkState(this.type == Component.Type.FILE, "Only component of type FILE have a FileAttributes object");
    return fileAttributes;
  }

  public static class FileAttributesImpl implements FileAttributes {

    public FileAttributesImpl(String languageKey, boolean unitTest) {
      this.languageKey = languageKey;
      this.unitTest = unitTest;
    }

    private final boolean unitTest;
    private final String languageKey;

    @Override
    public boolean isUnitTest() {
      return unitTest;
    }

    @Override
    public String getLanguageKey() {
      return languageKey;
    }
  }
}
