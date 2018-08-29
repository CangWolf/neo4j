/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * Neo4j object code can be licensed independently from the source
 * under separate terms from the AGPL. Inquiries can be directed to:
 * licensing@neo4j.com
 *
 * More information is also available at:
 * https://neo4j.com/licensing/
 */
package org.neo4j.causalclustering.messaging.marshalling.v2.decoding;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.neo4j.causalclustering.messaging.marshalling.CoreReplicatedContentMarshal;

public class ReplicatedContentChunkDecoder extends ByteToMessageDecoder
{
    private final CoreReplicatedContentMarshal contentMarshal = new CoreReplicatedContentMarshal();

    ReplicatedContentChunkDecoder()
    {
        setCumulator( new ContentChunkCumulator() );
    }

    @Override
    protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
    {
        in.markReaderIndex();
        boolean isLast = in.readBoolean();
        if ( isLast )
        {
            out.add( contentMarshal.unmarshalContent( in.readByte(), in ) );
        }
        else
        {
            in.resetReaderIndex();
        }
    }

    private static class ContentChunkCumulator implements Cumulator
    {
        @Override
        public ByteBuf cumulate( ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in )
        {
            boolean isLast = in.readBoolean();
            cumulation.setBoolean( 0, isLast );
            return COMPOSITE_CUMULATOR.cumulate( alloc, cumulation, in.slice() );
        }
    }
}