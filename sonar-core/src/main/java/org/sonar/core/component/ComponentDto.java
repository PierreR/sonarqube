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
package org.sonar.core.component;

import org.sonar.api.component.Component;
import org.sonar.core.persistence.Dto;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.util.Date;

public class ComponentDto extends Dto<String> implements Component {

  private Long id;
  private String uuid;
  private String kee;
  private String scope;
  private String qualifier;

  private String projectUuid;
  private String moduleUuid;
  private String moduleUuidPath;

  private String path;
  private String deprecatedKey;
  private String name;
  private String longName;
  private String language;
  private Long subProjectId;
  private boolean enabled = true;
  private Date authorizationUpdatedAt;

  public Long getId() {
    return id;
  }

  public ComponentDto setId(Long id) {
    this.id = id;
    return this;
  }

  public String uuid() {
    return uuid;
  }

  public ComponentDto setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public String key() {
    return kee;
  }

  public String scope() {
    return scope;
  }

  public ComponentDto setScope(String scope) {
    this.scope = scope;
    return this;
  }

  @Override
  public String qualifier() {
    return qualifier;
  }

  public ComponentDto setQualifier(String qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  @CheckForNull
  public String deprecatedKey() {
    return deprecatedKey;
  }

  public ComponentDto setDeprecatedKey(@Nullable String deprecatedKey) {
    this.deprecatedKey = deprecatedKey;
    return this;
  }

  /**
   * Return the root project id. On a root project, return itself
   */
  public String projectUuid() {
    return projectUuid;
  }

  public ComponentDto setProjectUuid(String projectUuid) {
    this.projectUuid = projectUuid;
    return this;
  }

  /**
   * Return the direct module of a component. Will be null on projects
   */
  @CheckForNull
  public String moduleUuid() {
    return moduleUuid;
  }

  public ComponentDto setModuleUuid(@Nullable String moduleUuid) {
    this.moduleUuid = moduleUuid;
    return this;
  }

  /**
   * Return the path from the project to the last modules
   */
  @CheckForNull
  public String moduleUuidPath() {
    return moduleUuidPath;
  }

  public ComponentDto setModuleUuidPath(@Nullable String moduleUuidPath) {
    this.moduleUuidPath = moduleUuidPath;
    return this;
  }

  @CheckForNull
  @Override
  public String path() {
    return path;
  }

  public ComponentDto setPath(@Nullable String path) {
    this.path = path;
    return this;
  }

  @Override
  public String name() {
    return name;
  }

  public ComponentDto setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public String longName() {
    return longName;
  }

  public ComponentDto setLongName(String longName) {
    this.longName = longName;
    return this;
  }

  @CheckForNull
  public String language() {
    return language;
  }

  public ComponentDto setLanguage(@Nullable String language) {
    this.language = language;
    return this;
  }

  @CheckForNull
  public Long subProjectId() {
    return subProjectId;
  }

  public ComponentDto setSubProjectId(@Nullable Long subProjectId) {
    this.subProjectId = subProjectId;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public ComponentDto setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * Only available on projects
   */
  @CheckForNull
  public Date getAuthorizationUpdatedAt() {
    return authorizationUpdatedAt;
  }

  public ComponentDto setAuthorizationUpdatedAt(@Nullable Date authorizationUpdatedAt) {
    this.authorizationUpdatedAt = authorizationUpdatedAt;
    return this;
  }

  @Override
  public String getKey() {
    return key();
  }

  public ComponentDto setKey(String key) {
    this.kee = key;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ComponentDto that = (ComponentDto) o;

    if (!id.equals(that.id)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}