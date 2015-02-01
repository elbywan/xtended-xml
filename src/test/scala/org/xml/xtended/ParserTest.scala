package org.xml.xtended

import org.scalatest.{Matchers, FlatSpec}
import org.xml.xtended.core.XtendedXmlParser
import org.xml.xtended.core.XtendedXmlParser._

import scala.io.Source
import scala.xml.pull._

class ParserTest extends FlatSpec with Matchers {

    val xml = Source.fromURL(getClass.getResource("/test.xml"))
    val parser = new XtendedXmlParser(xml)

    def goToNextBook() = parser.goToByName[EvElemStart](new TagOpened("book"))
    def getBookId(eltOpt : Option[EvElemStart]) = eltOpt.get.attrs.asAttrMap.get("id").get


    "An Xtended parser" should "find the book pointed by the id bk102" in {
        def findingLoop(eltOpt: Option[EvElemStart], accu: Int): Unit = {
            if (accu == 0) {
                eltOpt.isDefined shouldBe true
                getBookId(eltOpt) should equal("bk101")
                findingLoop(goToNextBook(), accu + 1)
            } else if (accu == 1) {
                eltOpt.isDefined shouldBe true
                getBookId(eltOpt) should equal("bk102")
            }
        }
        findingLoop(goToNextBook(), 0)
    }

    it should "parse the book details" in {
        val book : EvElemStart = parser.element_read.asInstanceOf[EvElemStart]
        getBookId(Some(book)) should equal ("bk102")

        parser.readNextTag("author").get should equal ("<author>Ralls, Kim</author>")
        parser.readNextTag("title").get should equal ("<title>Midnight Rain</title>")
        parser.readNextTag("genre").get should equal ("<genre>Fantasy</genre>")
        parser.readNextTag("price").get should equal ("<price>5.95</price>")
        parser.readNextTag("publish_date").get should equal ("<publish_date>2000-12-16</publish_date>")
        parser.readNextTag("description").get should equal ("""<description>A former architect battles corporate zombies,
                                                          |            an evil sorceress, and her own childhood to become queen
                                                          |            of the world.</description>""".stripMargin)
    }


}
