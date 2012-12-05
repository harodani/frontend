package project.cs.netinfservice.netinf.node.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.search.impl.events.SearchServiceResultEvent;
import project.cs.netinfservice.application.MainNetInfApplication;
import project.cs.netinfservice.database.DatabaseException;
import project.cs.netinfservice.database.IODatabase;
import project.cs.netinfservice.database.IODatabaseFactory;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import android.util.Log;

import com.google.inject.Inject;

/**
 * Enables search for a URL in the local SQLite DB and remote NRS.
 * @author Linus Sunde
 * @author Thiago Costa Porto
 */
public class SearchServiceSQLite implements SearchService {

    /** Log Tag. */
    private static final String TAG = "SearchServiceSQLite";

    /** DatamodelFactory used to create identifiers. */
    private DatamodelFactory mDatamodelFactory;
    /** The identity of a certain instance of this class. */
    private SearchServiceIdentityObject mIdentityObject;
    /** The local SQLite DB. */
    private IODatabase mDatabase;

    /**
     * Creates a new instance of this class.
     * @param datamodelFactory
     *      The DatamodelFactory to use when creating identifiers
     * @param databaseFactory
     *      The IODatabaseFactory to use when accessing the local SQLite DB
     */
    @Inject
    public SearchServiceSQLite(final DatamodelFactory datamodelFactory, IODatabaseFactory databaseFactory) {
        mDatamodelFactory = datamodelFactory;
        mDatabase = databaseFactory.create(MainNetInfApplication.getAppContext());
    }

    @Override
    public void getByQueryTemplate(String type, List<String> url, int searchId,
            SearchServiceIdentityObject searchIdentity, SearchController searchController) {
        // type should be "url"
        // url should be a list of 1 url
        // searchId
        // searchIdentity
        // searchController

        // TODO Make this able to return Set<Identifier>
        // TODO Get hash algorithm from database, not hardcoded
        // TODO Make sure metadata is correctly added

        Set<Identifier> resultSet = new HashSet<Identifier>();
        try {

            SearchResult searchResult = mDatabase.searchIO(url.get(0));

            Identifier identifier = mDatamodelFactory.createIdentifier();

            // Add Hash
            IdentifierLabel hashLabel = mDatamodelFactory.createIdentifierLabel();
            hashLabel.setLabelName(SailDefinedLabelName.HASH_CONTENT.getLabelName());
            hashLabel.setLabelValue(searchResult.getHash());
            identifier.addIdentifierLabel(hashLabel);

            // Add Hash Algorithm
            IdentifierLabel hashAlgLabel = mDatamodelFactory.createIdentifierLabel();
            hashAlgLabel.setLabelName(SailDefinedLabelName.HASH_ALG.getLabelName());
            hashAlgLabel.setLabelValue("sha-256");
            identifier.addIdentifierLabel(hashAlgLabel);

            // Add Metadata
            IdentifierLabel metaLabel = mDatamodelFactory.createIdentifierLabel();
            metaLabel.setLabelName(SailDefinedLabelName.META_DATA.getLabelName());
            metaLabel.setLabelValue(searchResult.getMetaData().convertToString());
            identifier.addIdentifierLabel(metaLabel);

            // Add the result
            resultSet.add(identifier);

        } catch (DatabaseException e) {

            Log.w(TAG, e.getMessage() != null ? e.getMessage() : "Search in local db failed");

        }

        searchController.handleSearchEvent(new SearchServiceResultEvent(
                "search result of " + getIdentityObject().getName(),
                searchId,
                searchIdentity,
                resultSet));

    }

    @Override
    public void getBySPARQL(String query, int searchId,
            SearchServiceIdentityObject searchIdentity, SearchController searchController) {
        // NOT SUPPORTED
        searchController.handleSearchEvent(new SearchServiceResultEvent(
                "search result of " + getIdentityObject().getName(),
                searchId,
                searchIdentity,
                new HashSet<Identifier>()));
    }

    // TODO Make sure we can always return true here. Is the DB really ready?
    // We think it should be
    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public SearchServiceIdentityObject getIdentityObject() {
        if (mIdentityObject == null) {
            mIdentityObject = createIdentityObject();
        }
        return mIdentityObject;
    }

    /**
     * Creates a new IdentityObject.
     * @return
     *      The created IdentityObject
     */
    private SearchServiceIdentityObject createIdentityObject() {
        SearchServiceIdentityObject idO = mDatamodelFactory.createSearchServiceIdentityObject();
        idO.setIdentifier(mDatamodelFactory.createIdentifier());
        idO.setName("SearchServiceSQLLite");
        idO.setDescription("This Search Service can be used to search in the local SQLLite DB.");
        return idO;
    }

    @Override
    public String describe() {
        return "the local SQLite DB";
    }

}
