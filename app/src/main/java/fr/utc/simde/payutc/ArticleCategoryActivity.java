package fr.utc.simde.payutc;

import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Samy on 27/10/2017.
 */

public class ArticleCategoryActivity extends ArticleGroupActivity {
    private static final String LOG_TAG = "_ArticleCategoryActivit";

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void createGroups(final JsonNode categoryList, final JsonNode articleList) throws Exception {
        HashMap<Integer, ArrayList<JsonNode>> articlesPerCategory = new HashMap<Integer, ArrayList<JsonNode>>();
        final int foundationId = nemopaySession.getFoundationId();
        final JsonNode authorizedList = config.getGroupList();
        Log.d(LOG_TAG, authorizedList.toString());

        for (final JsonNode article : articleList) {
            if (!article.has("id") || !article.has("price") || !article.has("name") || !article.has("active") || !article.has("cotisant") || !article.has("alcool") || !article.has("categorie_id") || !article.has("image_url") || !article.has("fundation_id") || article.get("fundation_id").intValue() != foundationId)
                throw new Exception("Unexpected JSON");

            if (!article.get("active").booleanValue())
                continue;

            if (articlesPerCategory.containsKey(article.get("categorie_id").intValue()))
                articlesPerCategory.get(article.get("categorie_id").intValue()).add(article);
            else
                articlesPerCategory.put(article.get("categorie_id").intValue(), new ArrayList<JsonNode>(){{ add(article); }});
        }

        for (JsonNode category : categoryList) {
            if (!category.has("id") || !category.has("name") || !category.has("fundation_id") || category.get("fundation_id").intValue() != foundationId)
                throw new Exception("Unexpected JSON");

            Log.d(LOG_TAG, category.toString());
            ArrayList<JsonNode> articlesForThisCategory = articlesPerCategory.get(category.get("id").intValue());
            if (config.getFoundationId() != -1) if (!authorizedList.has(Integer.toString(category.get("id").intValue())))
                continue;
            else if (articlesForThisCategory == null || articlesForThisCategory.size() == 0)
                continue;

            createNewGroup(category.get("name").textValue(), (ArrayNode) new ObjectMapper().readTree(articlesForThisCategory.toString()));
        }
    }
}
