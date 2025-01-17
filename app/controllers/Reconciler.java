package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Resource;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import services.QueryContext;
import services.SearchConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author pvb
 */
public class Reconciler extends OERWorldMap {

  @Inject
  FormFactory formFactory;

  @Inject
  public Reconciler(Configuration aConf, Environment aEnv) {
    super(aConf, aEnv);
  }

  public Result meta(String aCallback) {
    ObjectNode result = Json.newObject();
    result.put("name", "oerworldmap reconciliation");
    result.put("identifierSpace", "https://capacity-exchange.wmcloud.org/resource");
    result.put("schemaSpace", "http://schema.org");
    ArrayNode defaultTypes = Json.newArray();
    Resource.mIdentifiedTypes.forEach(x -> {
      ObjectNode defaultType = Json.newObject();
      defaultType.put("id", x);
      defaultType.put("name", x);
      defaultTypes.add(defaultType);
    });
    result.set("defaultTypes", defaultTypes);
    result.set("view", Json.newObject().put("url", "https://capacity-exchange.wmcloud.org/resource/{{id}}"));
    return StringUtils.isEmpty(aCallback) ? ok(result)
      : ok(String.format("/**/%s(%s);", aCallback, result.toString()));
  }


  public Result reconcile() {
    DynamicForm requestData = formFactory.form().bindFromRequest();
    JsonNode request = Json.parse(requestData.get("queries"));
    Iterator<Map.Entry<String, JsonNode>> inputQueries = request.fields();
    return ok(reconcile(inputQueries, null, Locale.ENGLISH)); // TODO: fetch Locale from UI
  }


  public JsonNode reconcile(final Iterator<Map.Entry<String, JsonNode>> aInputQueries,
    final QueryContext aQueryContext, final Locale aPreferredLocale) {
    QueryContext queryContext = aQueryContext != null ? aQueryContext : getQueryContext();
    String searchConfigFile = mConf.getString("reconcile.conf.file");
    SearchConfig searchConfig = searchConfigFile != null
      ? new SearchConfig(searchConfigFile)
      : new SearchConfig();
    queryContext.setElasticsearchFieldBoosts(searchConfig.getBoostsForElasticsearch());
    ObjectNode response = Json.newObject();

    try {
      while (aInputQueries.hasNext()) {
        Map.Entry<String, JsonNode> inputQuery = aInputQueries.next();
        JsonNode limitNode = inputQuery.getValue().get("limit");
        int limit = limitNode == null ? -1 : limitNode.asInt();
        String queryString = inputQuery.getValue().get("query").asText();
        JsonNode type = inputQuery.getValue().get("type");
        Map<String, List<String>> typeFilter = new HashMap<>();
        if (type != null) {
          typeFilter.put("about.@type", Arrays.asList(type.asText()));
        }
        JsonNode reconciled = mBaseRepository
          .reconcile(queryString, 0, limit, null, typeFilter, queryContext, aPreferredLocale);
        response.set(inputQuery.getKey(), reconciled);
      }
    } catch (IOException ioe) {
      Logger.error("Could not query base repository.", ioe);
    }
    return response;
  }
}
