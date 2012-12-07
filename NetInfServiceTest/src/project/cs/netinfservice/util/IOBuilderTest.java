package project.cs.netinfservice.util;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfservice.util.metadata.Metadata;
import project.cs.netinfservice.util.metadata.MetadataParser;
import android.test.AndroidTestCase;


/**
 * Tests the IOBuilder.
 * 
 * @author Kim-Anh Tran
 *
 */
public class IOBuilderTest extends AndroidTestCase {
	
	/** The hash. */
	private static final String HASH = "111";
	
	/** The hash algorithm. */
	private static final String HASH_ALG = "sha-256";
	
	/** The content type. */
	private static final String CONTENT_TYPE = "text/plain";
	
	/** The file path. */
	private static final String FILE_PATH = "/home/lisa/url.txt";
	
	/** the file size. */
	private static final String FILE_SIZE = "11";
	
	/** The first url corresponding to the object. */
	private static final String URL_1 = "www.dn.se";
	
	/** The second url corresponding to the object. */
	private static final String URL_2 = "www.svt.se";
	
	/** The third url corresponding to the object. */
	private static final String URL_3 = "www.google.se";
	
	/** Meta-data label for the filepath. */
	private String LABEL_FILEPATH;
	
	/** Meta-data label for the file size. */
	private String LABEL_FILESIZE;
	
	/** Meta-data label for the url. */
	private String LABEL_URL;

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		UProperties instance = UProperties.INSTANCE;
		LABEL_FILEPATH = instance.getPropertyWithName("metadata.filepath");
		LABEL_FILESIZE = instance.getPropertyWithName("metadata.filesize");
		LABEL_URL = instance.getPropertyWithName("metadata.url");
	}
	
	/** Tests building an IO from scratch. */
	public void testBuild() {
		
		IOBuilder builder = new IOBuilder(new DatamodelFactoryImpl());
		
		InformationObject io = builder.addMetaData(LABEL_FILEPATH, FILE_PATH)
									.addMetaData(LABEL_FILESIZE, FILE_SIZE)
									.addMetaData(LABEL_URL, URL_1)
									.addMetaData(LABEL_URL, URL_2)
									.setContentType(CONTENT_TYPE)
									.setHash(HASH)
									.setHashAlgorithm(HASH_ALG).build();
		
		assertCorrectIo(io);
		
	}

	
	private void assertCorrectIo(InformationObject io) {
		Identifier identifier = io.getIdentifier();

		String actualContentType = identifier.getIdentifierLabel(
				SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue();
		String actualHash = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		String actualHashAlg = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
		
		assertEquals(actualContentType, CONTENT_TYPE);
		assertEquals(actualHash, HASH);
		assertEquals(actualHashAlg, HASH_ALG);
		
		// Check if meta data was created correctly.
		String metadata = identifier.getIdentifierLabel(
				SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();
		
		Object jsonMeta = JSONValue.parse(metadata);
		if (!(jsonMeta instanceof JSONObject)) {
			Assert.fail("Should be a jsonobject.");
		}
		
		Map<String, Object>  map = null;
		try {
			map = MetadataParser.toMap((JSONObject) jsonMeta);
		} catch (JSONException e) {
			Assert.fail("Should not have thrown an exception.");
		}
		String actualFilepath = (String) map.get(LABEL_FILEPATH);
		String actualFilesize = (String) map.get(LABEL_FILESIZE);
		List<String> urlList = (List<String>) map.get(LABEL_URL);
		
		assertEquals(FILE_PATH, actualFilepath);
		assertEquals(FILE_SIZE, actualFilesize);
		assertTrue(urlList.size() == 2);
		assertTrue(urlList.contains(URL_1));
		assertTrue(urlList.contains(URL_2));
	}
}
