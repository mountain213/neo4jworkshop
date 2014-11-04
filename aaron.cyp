CREATE (aaron:Person:Awesome {name:"Aaron"})
CREATE (brian:Person {name:"Brian"})
CREATE (christie:Person {name:"Christie"})
CREATE (nick:Person {name:"Nick"})
CREATE (aaron)-[:KNOWS]->(brian)
CREATE (aaron)-[:KNOWS]->(christie)

CREATE (republic:Book {title:"The Republic"})
CREATE (christie)-[:FREQUENTLY_BEATS_UP]->(nick)
CREATE (aaron)-[:HAS_READ]->(republic)
CREATE (brian)-[:HAS_READ]->(republic)

RETURN aaron

;

