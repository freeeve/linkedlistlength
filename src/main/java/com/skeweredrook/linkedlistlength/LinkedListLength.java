package com.skeweredrook.linkedlistlength;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.impl.traversal.TraversalDescriptionImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.nio.charset.Charset;
import java.util.Comparator;

@Path( "/" )
public class LinkedListLength
{
	private static final DynamicRelationshipType COMMENTS = DynamicRelationshipType.withName( "COMMENTS" );

	@GET
	@Produces( MediaType.TEXT_PLAIN )
	@Path( "/{nodeId}" )
	public Response length(@Context GraphDatabaseService db, @PathParam( "nodeId" ) long nodeId )
	{
		int length = 0;
		Transaction tx = db.beginTx();
		try {
			Node startNode = db.getNodeById( nodeId );
			TraversalDescription td = new TraversalDescriptionImpl();
			Traverser traverse = td
				.depthFirst()
				.relationships( COMMENTS, Direction.OUTGOING )
				.sort( new Comparator<org.neo4j.graphdb.Path>()
				{
					@Override
					public int compare( org.neo4j.graphdb.Path o1, org.neo4j.graphdb.Path o2 )
					{
						return Integer.valueOf( o2.length() ).compareTo( o1 .length() );
					}
				} )
				.traverse( startNode );

			length = traverse.iterator().next().length();

			tx.failure();
		} catch(Exception ex) {
			tx.failure();
		}
		return Response.status( Status.OK ).entity(
			("" + length).getBytes(Charset.forName("UTF-8"))).build();
	}
}