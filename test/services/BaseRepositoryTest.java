package services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.geo.GeoPoint;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import helpers.ElasticsearchTestGrid;
import helpers.JsonLdConstants;
import helpers.JsonTest;
import models.Resource;
import services.repository.BaseRepository;

public class BaseRepositoryTest extends ElasticsearchTestGrid implements JsonTest {
  
  private static BaseRepository mBaseRepo = new BaseRepository(mConfig);

  @Test
  public void testResourceWithIdentifiedSubObject() throws IOException {
    Resource resource = new Resource("Person", "id001");
    String property = "attended";
    Resource value = new Resource("Event", "OER15");
    resource.put(property, value);
    Resource expected1 = getResourceFromJsonFile("BaseRepositoryTest/testResourceWithIdentifiedSubObject.OUT.1.json");
    Resource expected2 = getResourceFromJsonFile("BaseRepositoryTest/testResourceWithIdentifiedSubObject.OUT.2.json");
    mBaseRepo.addResource(resource);
    Assert.assertEquals(expected1, mBaseRepo.getResource("id001"));
    Assert.assertEquals(expected2, mBaseRepo.getResource("OER15"));
    mBaseRepo.deleteResource("id001");
    mBaseRepo.deleteResource("OER15");
  }

  @Test
  public void testResourceWithUnidentifiedSubObject() throws IOException {
    Resource resource = new Resource("Person", "id002");
    Resource value = new Resource("Foo", null);
    resource.put("attended", value);
    Resource expected = getResourceFromJsonFile("BaseRepositoryTest/testResourceWithUnidentifiedSubObject.OUT.1.json");
    mBaseRepo.addResource(resource);
    Assert.assertEquals(expected, mBaseRepo.getResource("id002"));
    mBaseRepo.deleteResource("id002");
  }

  @Test
  public void testDeleteResourceWithMentionedResources() throws IOException {
    // setup: 1 Person ("in1") who has 2 affiliations
    Resource in = getResourceFromJsonFile("BaseRepositoryTest/testDeleteResourceWithMentionedResources.IN.1.json");
    Resource expected1 = getResourceFromJsonFile(
        "BaseRepositoryTest/testDeleteResourceWithMentionedResources.OUT.1.json");
    Resource expected2 = getResourceFromJsonFile(
        "BaseRepositoryTest/testDeleteResourceWithMentionedResources.OUT.2.json");
    List<Resource> denormalized = ResourceDenormalizer.denormalize(in, mBaseRepo);

    for (Resource resource : denormalized) {
      mBaseRepo.addResource(resource);
    }
    // delete affiliation "Oh No Company" and check whether it has been removed
    // from referencing resources
    Resource toBeDeleted = mBaseRepo.getResource("9m8n7b");
    mBaseRepo.deleteResource(toBeDeleted.getAsString(JsonLdConstants.ID));
    Resource result1 = mBaseRepo.getResource("4g5h6j");
    Resource result2 = mBaseRepo.getResource("1a2s3d");
    Assert.assertEquals(expected1, result1);
    Assert.assertEquals(expected2, result2);
    Assert.assertNull(mBaseRepo.getResource("9m8n7b"));
    mBaseRepo.deleteResource("4g5h6j");
    mBaseRepo.deleteResource("1a2s3d");
  }

  @Test
  public void testDeleteLastResourceInList() throws IOException {
    Resource db1 = getResourceFromJsonFile("BaseRepositoryTest/testDeleteLastResourceInList.DB.1.json");
    Resource db2 = getResourceFromJsonFile("BaseRepositoryTest/testDeleteLastResourceInList.DB.2.json");
    Resource out = getResourceFromJsonFile("BaseRepositoryTest/testDeleteLastResourceInList.OUT.1.json");
    mBaseRepo.addResource(db1);
    mBaseRepo.addResource(db2);
    mBaseRepo.deleteResource("urn:uuid:3a25e950-a3c0-425d-946d-9806665ec665");
    Assert.assertNull(mBaseRepo.getResource("urn:uuid:3a25e950-a3c0-425d-946d-9806665ec665"));
    Assert.assertEquals(out, mBaseRepo.getResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e25503"));
    mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e25503");
  }

  @Test
  public void testDeleteResourceFromList() throws IOException {
    Resource db1 = getResourceFromJsonFile("BaseRepositoryTest/testDeleteResourceFromList.DB.1.json");
    Resource db2 = getResourceFromJsonFile("BaseRepositoryTest/testDeleteResourceFromList.DB.2.json");
    Resource db3 = getResourceFromJsonFile("BaseRepositoryTest/testDeleteResourceFromList.DB.2.json");
    Resource out1 = getResourceFromJsonFile("BaseRepositoryTest/testDeleteResourceFromList.OUT.1.json");
    Resource out2 = getResourceFromJsonFile("BaseRepositoryTest/testDeleteResourceFromList.OUT.2.json");
    mBaseRepo.addResource(db1);
    mBaseRepo.addResource(db2);
    mBaseRepo.addResource(db3);
    mBaseRepo.deleteResource("urn:uuid:3a25e950-a3c0-425d-946d-9806665ec665");
    Assert.assertNull(mBaseRepo.getResource("urn:uuid:3a25e950-a3c0-425d-946d-9806665ec665"));
    Assert.assertEquals(out1, mBaseRepo.getResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e25503"));
    Assert.assertEquals(out2, mBaseRepo.getResource("urn:uuid:7cfb9aab-1a3f-494c-8fb1-64755faf180c"));
    mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e25503");
    mBaseRepo.deleteResource("urn:uuid:7cfb9aab-1a3f-494c-8fb1-64755faf180c");
  }

  @Test
  public void testRemoveReference() throws IOException {
    Resource in = getResourceFromJsonFile("BaseRepositoryTest/testRemoveReference.IN.json");
    Resource db1 = getResourceFromJsonFile("BaseRepositoryTest/testRemoveReference.DB.1.json");
    Resource db2 = getResourceFromJsonFile("BaseRepositoryTest/testRemoveReference.DB.2.json");
    Resource out1 = getResourceFromJsonFile("BaseRepositoryTest/testRemoveReference.OUT.1.json");
    Resource out2 = getResourceFromJsonFile("BaseRepositoryTest/testRemoveReference.OUT.2.json");
    mBaseRepo.addResource(db1);
    mBaseRepo.addResource(db2);
    mBaseRepo.addResource(in);
    Resource get1 = mBaseRepo.getResource(out1.getAsString(JsonLdConstants.ID));
    Resource get2 = mBaseRepo.getResource(out2.getAsString(JsonLdConstants.ID));
    assertEquals(out1, get1);
    assertEquals(out2, get2);
    mBaseRepo.deleteResource(out1.getAsString(JsonLdConstants.ID));
    mBaseRepo.deleteResource(out2.getAsString(JsonLdConstants.ID));
  }

  @Test
  public void testGetResourcesWithWildcard() throws IOException {
    Resource in1 = getResourceFromJsonFile("BaseRepositoryTest/testGetResourcesWithWildcard.DB.1.json");
    Resource in2 = getResourceFromJsonFile("BaseRepositoryTest/testGetResourcesWithWildcard.DB.2.json");
    mBaseRepo.addResource(in1);
    mBaseRepo.addResource(in2);
    Assert.assertEquals(2, mBaseRepo.getResources("\\*.@id", "123").size());
    mBaseRepo.deleteResource(in1.getAsString(JsonLdConstants.ID));
    mBaseRepo.deleteResource(in2.getAsString(JsonLdConstants.ID));
  }

  @Test
  public void testSearchRankingNameHitsFirst() throws IOException, ParseException {

    Resource db1 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.1.json");
    Resource db2 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.2.json");
    Resource db3 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.3.json");
    Resource db4 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.4.json");
    Resource db5 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.5.json");
    Resource db6 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.6.json");
    Resource db7 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.7.json");
    Resource db8 = getResourceFromJsonFile("BaseRepositoryTest/testSearchRanking.DB.8.json");

    List<Resource> expectedList = new ArrayList<>();
    expectedList.add(db1);
    expectedList.add(db2);
    expectedList.add(db3);
    expectedList.add(db4);
    expectedList.add(db5);
    expectedList.add(db6);
    expectedList.add(db7);
    expectedList.add(db8);

    mBaseRepo.addResource(db7);
    mBaseRepo.addResource(db2);
    mBaseRepo.addResource(db6);
    mBaseRepo.addResource(db3);
    mBaseRepo.addResource(db4);
    mBaseRepo.addResource(db1);
    mBaseRepo.addResource(db5);
    mBaseRepo.addResource(db8);

    try {
      QueryContext queryContext = new QueryContext(null, null);
      queryContext.setElasticsearchFieldBoosts( //
          new String[] { //
              "about.name.@value^9", //
              "about.alternateName.@value^6" });
      List<Resource> actualList = mBaseRepo.query("oerworldmap", 0, 10, null, null, queryContext).getItems();
      List<String> actualNameList = getNameList(actualList);

      // must provide 7 hits
      Assert.assertTrue("Result size list is: " + actualNameList.size(), actualNameList.size() == 7);

      // hits 1 to 5 must contain "oerworldmap" in field "name".
      // hit 1 should be the Service named "OERWorldMap"
      for (int i = 0; i < 5; i++) {
        Assert.assertTrue(actualNameList.get(i).toLowerCase().contains("oerworldmap"));
      }
      // hits 5 and 6 must not contain "oerworldmap" in field "name"
      for (int i = 5; i < 7; i++) {
        Assert.assertFalse(actualNameList.get(i).toLowerCase().contains("oerworldmap"));
      }
      // Resources db6 must not be found, since it only contains "oerworldmap"
      // in the field url
      // that is not in the list of searchable fields
      Assert.assertFalse(actualNameList.contains("A Good Provider 6"));
      // Resources db8 must not be found, since it doesn't contain "oerworldmap"
      // in any searchable field
      // that is not in the list of searchable fields
      Assert.assertFalse(actualNameList.contains("A Good Provider 8"));
    } //
    finally {
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00001");
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00002");
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00003");
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00004");
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00005");
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00006");
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00007");
      mBaseRepo.deleteResource("urn:uuid:c7f5334a-3ddb-4e46-8653-4d8c01e00008");
      mBaseRepo.deleteResource("urn:uuid:3a25e950-a3c0-425d-946d-980666500001");
    }
  }

  @Test
  public void testZoomedQueryResults() throws IOException {
    Resource db1 = getResourceFromJsonFile("BaseRepositoryTest/testZoomedQueryResults.DB.1.json");
    Resource db2 = getResourceFromJsonFile("BaseRepositoryTest/testZoomedQueryResults.DB.2.json");
    Resource db3 = getResourceFromJsonFile("BaseRepositoryTest/testZoomedQueryResults.DB.3.json");

    mBaseRepo.addResource(db1);
    mBaseRepo.addResource(db2);
    mBaseRepo.addResource(db3);

    QueryContext queryContext = new QueryContext(null, null);

    // query before zooming
    List<Resource> beforeZoomList = mBaseRepo.query("Zoom", 0, 10, null, null, queryContext).getItems();
    Assert.assertTrue(beforeZoomList.size() == 3);
    List<String> beforeZoomNames = getNameList(beforeZoomList);
    Assert.assertTrue(beforeZoomNames.contains("In Zoom Organization 1"));
    Assert.assertTrue(beforeZoomNames.contains("In Zoom Organization 2"));
    Assert.assertTrue(beforeZoomNames.contains("Out Of Zoom Organization 3"));

    // "zoom"
    queryContext.setZoomTopLeft(new GeoPoint(8.0, 2.5));
    queryContext.setZoomBottomRight(new GeoPoint(4.0, 8.0));

    // query after zooming
    List<Resource> afterZoomList = mBaseRepo.query("Zoom", 0, 10, null, null, queryContext).getItems();
    Assert.assertTrue(afterZoomList.size() == 2);
    List<String> afterZoomNames = getNameList(afterZoomList);
    Assert.assertTrue(afterZoomNames.contains("In Zoom Organization 1"));
    Assert.assertTrue(afterZoomNames.contains("In Zoom Organization 2"));
    Assert.assertFalse(afterZoomNames.contains("Out Of Zoom Organization 3"));

    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0001");
    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0002");
    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0003");
  }
  
  @Test
  public void testPolygonFilteredSearch() throws IOException {
    Resource db1 = getResourceFromJsonFile("BaseRepositoryTest/testPolygonFilteredSearch.DB.1.json");
    Resource db2 = getResourceFromJsonFile("BaseRepositoryTest/testPolygonFilteredSearch.DB.2.json");
    Resource db3 = getResourceFromJsonFile("BaseRepositoryTest/testPolygonFilteredSearch.DB.3.json");

    mBaseRepo.addResource(db1);
    mBaseRepo.addResource(db2);
    mBaseRepo.addResource(db3);

    QueryContext queryContext = new QueryContext(null, null);

    // query before filtering
    List<Resource> beforeFilterList = mBaseRepo.query("Polygon", 0, 10, null, null, queryContext).getItems();
    Assert.assertTrue(beforeFilterList.size() == 3);
    List<String> beforeFilterNames = getNameList(beforeFilterList);
    Assert.assertTrue(beforeFilterNames.contains("Out Of Polygon Organization 1"));
    Assert.assertTrue(beforeFilterNames.contains("In Polygon Organization 2"));
    Assert.assertTrue(beforeFilterNames.contains("In Polygon Organization 3"));

    // filter into polygon
    List<GeoPoint> polygon = new ArrayList<>();
    polygon.add(new GeoPoint(12.0, 13.0));
    polygon.add(new GeoPoint(12.0, 14.0));
    polygon.add(new GeoPoint(11.0, 14.0));
    polygon.add(new GeoPoint(6.0, 4.0));
    polygon.add(new GeoPoint(6.0, 3.0));
    polygon.add(new GeoPoint(7.0, 3.0));
    queryContext.setPolygonFilter(polygon);
    
    // query after filtering
    List<Resource> afterFilterList = mBaseRepo.query("Polygon", 0, 10, null, null, queryContext).getItems();
    Assert.assertTrue(afterFilterList.size() == 2);
    List<String> afterFilterNames = getNameList(afterFilterList);
    Assert.assertFalse(afterFilterNames.contains("Out Of Polygon Organization 1"));
    Assert.assertTrue(afterFilterNames.contains("In Polygon Organization 2"));
    Assert.assertTrue(afterFilterNames.contains("In Polygon Organization 3"));

    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0001");
    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0002");
    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0003");
  }
  
  @Test
  public void testZoomedPolygonQueryResults() throws IOException {
    Resource db1 = getResourceFromJsonFile("BaseRepositoryTest/testZoomedPolygonQueryResults.DB.1.json");
    Resource db2 = getResourceFromJsonFile("BaseRepositoryTest/testZoomedPolygonQueryResults.DB.2.json");
    Resource db3 = getResourceFromJsonFile("BaseRepositoryTest/testZoomedPolygonQueryResults.DB.3.json");

    mBaseRepo.addResource(db1);
    mBaseRepo.addResource(db2);
    mBaseRepo.addResource(db3);

    QueryContext queryContext = new QueryContext(null, null);

    // query before zooming
    List<Resource> beforeFilterList = mBaseRepo.query("Polygon Zoom", 0, 10, null, null, queryContext).getItems();
    Assert.assertTrue(beforeFilterList.size() == 3);
    List<String> beforeFilterNames = getNameList(beforeFilterList);
    Assert.assertTrue(beforeFilterNames.contains("Out Of Polygon Zoom Organization 1"));
    Assert.assertTrue(beforeFilterNames.contains("In Polygon Zoom Organization 2"));
    Assert.assertTrue(beforeFilterNames.contains("Out Of Polygon Zoom Organization 3"));

    // filter into polygon
    List<GeoPoint> polygon = new ArrayList<>();
    polygon.add(new GeoPoint(12.0, 13.0));
    polygon.add(new GeoPoint(12.0, 14.0));
    polygon.add(new GeoPoint(11.0, 14.0));
    polygon.add(new GeoPoint(6.0, 4.0));
    polygon.add(new GeoPoint(6.0, 3.0));
    polygon.add(new GeoPoint(7.0, 3.0));
    queryContext.setPolygonFilter(polygon);
    
    // and
    
    // "zoom"
    queryContext.setZoomTopLeft(new GeoPoint(8.0, 2.5));
    queryContext.setZoomBottomRight(new GeoPoint(4.0, 8.0));

    // query after zooming
    List<Resource> afterFilterList = mBaseRepo.query("Polygon", 0, 10, null, null, queryContext).getItems();
    Assert.assertTrue(afterFilterList.size() == 1);
    List<String> afterFilterNames = getNameList(afterFilterList);
    Assert.assertFalse(afterFilterNames.contains("Out Of Polygon Zoom Organization 1"));
    Assert.assertTrue(afterFilterNames.contains("In Polygon Zoom Organization 2"));
    Assert.assertFalse(afterFilterNames.contains("Out Of Polygon Zoom Organization 3"));

    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0001");
    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0002");
    mBaseRepo.deleteResource("urn:uuid:eea2cb2a-9f4c-11e5-945f-001999ac0003");
  }

  private List<String> getNameList(List<Resource> aResourceList) {
    List<String> result = new ArrayList<>();
    for (Resource r : aResourceList) {
      List<?> nameList = (List<?>) r.get("name");
      Resource name = (Resource) nameList.get(0);
      result.add(name.getAsString("@value"));
    }
    return result;
  }

}
