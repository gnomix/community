package org.neo4j.cypher.parser

/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import org.neo4j.cypher._
import org.neo4j.cypher.commands._
import scala.util.parsing.combinator._

class CypherParser extends JavaTokenParsers with StartClause with MatchClause with WhereClause with ReturnClause with SkipLimitClause with OrderByClause {

  def query: Parser[Query] = start ~ opt(matching) ~ opt(where) ~ returns ~ opt(order) ~ opt(skip) ~ opt(limit) ^^ {
    case start ~ matching ~ where ~ returns ~ sort ~ None ~ None => Query(returns._1, start, matching, where, returns._2, sort, None)
    case start ~ matching ~ where ~ returns ~ sort ~ skip ~ limit => Query(returns._1, start, matching, where, returns._2, sort, Some(Slice(skip, limit)))
  }

  @throws(classOf[SyntaxError])
  def parse(queryText: String): Query =
    parseAll(query, queryText) match {
      case Success(r, q) => r
      case NoSuccess(message, input) => message match {
        case "string matching regex `-?\\d+' expected but `)' found" => throw new SyntaxError("Last element of list must be a value")
        case _ => throw new SyntaxError(message)
      }
    }

}