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

package org.sonar.server.issue.filter;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Resources;
import java.util.Comparator;
import java.util.List;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.db.issue.IssueFilterDto;
import org.sonar.server.user.UserSession;

public class SearchAction implements IssueFilterWsAction {

  private final IssueFilterService service;
  private final IssueFilterJsonWriter issueFilterJsonWriter;
  private final UserSession userSession;

  public SearchAction(IssueFilterService service, IssueFilterJsonWriter issueFilterJsonWriter, UserSession userSession) {
    this.service = service;
    this.issueFilterJsonWriter = issueFilterJsonWriter;
    this.userSession = userSession;
  }

  @Override
  public void define(WebService.NewController controller) {
    WebService.NewAction action = controller.createAction("search");
    action
      .setDescription("Get the list of favorite issue filters.")
      .setInternal(false)
      .setHandler(this)
      .setSince("5.2")
      .setResponseExample(Resources.getResource(this.getClass(), "example-search.json"));
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    JsonWriter json = response.newJsonWriter();
    json.beginObject();

    // Favorite filters, if logged in
    if (userSession.isLoggedIn()) {
      List<IssueFilterDto> filters = service.findFavoriteFilters(userSession);
      List<IssueFilterDto> sharedFiltersWithoutUserFilters = service.findSharedFiltersWithoutUserFilters(userSession);
      filters.addAll(sharedFiltersWithoutUserFilters);
      ImmutableSortedSet<IssueFilterDto> allUniqueIssueFilters = FluentIterable.from(filters).toSortedSet(IssueFilterDtoIdComparator.INSTANCE);
      json.name("issueFilters").beginArray();
      for (IssueFilterDto favorite : allUniqueIssueFilters) {
        issueFilterJsonWriter.write(json, favorite, userSession);
      }
      json.endArray();
    }

    json.endObject();
    json.close();
  }

  private enum IssueFilterDtoIdComparator implements Comparator<IssueFilterDto> {
    INSTANCE;

    @Override
    public int compare(IssueFilterDto o1, IssueFilterDto o2) {
      return o1.getId().intValue() - o2.getId().intValue();
    }
  }
}
