package com.ironbucket.claimspindel;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ironbucket.pactumscroll.TokenUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Usage in application.yml:
 * predicates:
 *   - Claims=region,east
 */
@Component
@Slf4j
class ClaimsRoutePredicateFactory extends AbstractRoutePredicateFactory<ClaimsRoutePredicateFactory.Config> {

	public ClaimsRoutePredicateFactory() {
		super(Config.class);
	}

	@Override
	public Predicate<ServerWebExchange> apply(Config config) {
		return exchange -> {
			String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
			String path = exchange.getRequest().getURI().getPath();
			log.debug("Request path: " + path+" - "+authHeader);
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return false;
			}
			String claimName=config.claimName();
			JsonNode claims = TokenUtils.getClaims(exchange);
			boolean hasRole = false;
			log.debug("authenticated");
			if(claims != null && "role".equals(claimName))  {                    
				ArrayNode roles = ((ArrayNode)claims.get("realm_access").get("roles"));								
				for(JsonNode checkRoleNode: roles) {
					hasRole = checkRoleNode.asText().equals(config.expectedValue);
					if(hasRole) {
						break;
					}
				}				
			}
			return hasRole;
		};
	}
	@Override
	public List<String> shortcutFieldOrder() {
		return List.of("claimName", "expectedValue");
	}
	record Config (String claimName,  String expectedValue) {}
}
