package org.mkralik.lra.quickstart.hotel;

import io.narayana.lra.client.internal.proxy.nonjaxrs.LRAParticipantRegistry;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(WadlResource.class);
        register(HotelEndpoints.class);
        register(io.narayana.lra.filter.ClientLRARequestFilter.class);
        register(io.narayana.lra.filter.ClientLRAResponseFilter.class);
        register(io.narayana.lra.filter.FilterRegistration.class);
        register(io.narayana.lra.filter.ServerLRAFilter.class);
        register(new AbstractBinder(){
            @Override
            protected void configure() {
                bind(LRAParticipantRegistry.class)
                        .to(LRAParticipantRegistry.class);
            }
        });
    }
}
