package com.redhat.mapitspring.rest;


import com.redhat.mapitspring.data.MapData;
import com.redhat.mapitspring.model.MapPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collections;
import java.util.List;

@Component
@Path("/map")
public class MapEndpoint {
    @Autowired
    private MapData data;

    @GET
    @Path(("/airports"))
    @Produces("application/json")
    public List<MapPoint> airports() {
        return data.getAirports();
    }
}
