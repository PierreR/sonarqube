define([
  '../models/issue'
], function (Issue) {

  return Backbone.Collection.extend({
    model: Issue,

    url: function () {
      return baseUrl + '/api/issues/search';
    },

    _injectRelational: function (issue, source, baseField, lookupField) {
      var baseValue = issue[baseField];
      if (baseValue != null && _.size(source)) {
        var lookupValue = _.find(source, function (candidate) {
          return candidate[lookupField] === baseValue;
        });
        if (lookupValue != null) {
          Object.keys(lookupValue).forEach(function (key) {
            var newKey = baseField + key.charAt(0).toUpperCase() + key.slice(1);
            issue[newKey] = lookupValue[key];
          });
        }
      }
      return issue;
    },

    parse: function (r) {
      var that = this;

      this.paging = {
        p: r.p,
        ps: r.ps,
        total: r.total,
        maxResultsReached: r.p * r.ps >= r.total
      };

      return r.issues.map(function (issue) {
        issue = that._injectRelational(issue, r.components, 'component', 'key');
        issue = that._injectRelational(issue, r.components, 'project', 'key');
        issue = that._injectRelational(issue, r.components, 'subProject', 'key');
        issue = that._injectRelational(issue, r.rules, 'rule', 'key');
        issue = that._injectRelational(issue, r.users, 'assignee', 'login');
        issue = that._injectRelational(issue, r.users, 'reporter', 'login');
        issue = that._injectRelational(issue, r.actionPlans, 'actionPlan', 'key');
        return issue;
      });
    }
  });

});
