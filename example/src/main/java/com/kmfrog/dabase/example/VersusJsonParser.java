package com.kmfrog.dabase.example;

import com.kmfrog.dabase.data.extra.JsonParser;
import com.kmfrog.dabase.data.extra.json.JsonObjectParser;
import com.kmfrog.dabase.exception.ParserException;
import org.json.JSONObject;

/**
 * Created by dust on 6/14/16.
 */
public class VersusJsonParser implements JsonParser<Versus> {


    @Override
    public String getDataType() {
        return "NO_CLZ";
    }

    @Override
    public Versus parseObject(JSONObject jsonObject) throws ParserException {
        try {
            Versus vs = new Versus();
            vs.setCreatedBy(jsonObject.getString("createdBy"));
            return vs;
        }
        catch(Exception ex){
            throw new ParserException(ex);
        }
    }
}
