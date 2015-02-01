package org.xml.xtended.core

import scala.io.Source
import scala.language.implicitConversions
import scala.xml.EntityRef
import scala.xml.pull._

/**
 * XMLExtendedEventReader is an XML StAX parser extending the official Scala parser.
 */
object XtendedXmlParser {

    /////////////////
    //  TAGEVENTS  //
    /////////////////

    /**
     * Events pulled by the parser.
     */
    class TagEvent

    /**
     * Tag opened event.
     * @param name Tag name
     */
    case class TagOpened(name: String) extends TagEvent

    /**
     * Tag closed event.
     * @param name Tag name
     */
    case class TagClosed(name: String) extends TagEvent

    /**
     * Processing instruction event.
     * @param target Instruction target
     */
    case class ProcessingInstruction(target: String) extends TagEvent

    /////////////////
    //  IMPLICITS  //
    /////////////////

    implicit def adaptTagEvent(tagEvent: TagOpened) : EvElemStart = EvElemStart("", tagEvent.name, null, null)
    implicit def adaptTagEvent(tagEvent: TagClosed) : EvElemEnd = EvElemEnd("", tagEvent.name)
    implicit def adaptTagEvent(tagEvent: ProcessingInstruction) : EvProcInstr = EvProcInstr(tagEvent.target, "")

    /**
     * Converts an XML event to string (as events are elements of the XML grammar).
     * @param event Event to print
     * @return A string representation
     */
    def xmlToString(event: XMLEvent) : String = event match {
        case EvElemStart(pre, label, attrs, scope) => s"<${if (pre != null) pre+":" else ""}$label${attrs.toString}>"
        case EvElemEnd(pre, label) => s"</${if (pre != null) pre else ""}$label>"
        case EvText(text: String) => text
        case EvEntityRef(entity: String) => new EntityRef(entity).toString
        case EvProcInstr(target: String, text: String) => s"<?$target $text?>"
        case EvComment(text: String) => s"<!-$text-->"
        case null => ""
    }

    /////////////////
    // COMPARISONS //
    /////////////////

    /**
     * XML events comparison by name.
     *
     * <table>
     *      <thead>
     *         <tr>
     *              <th>Event name</th>
     *              <th>Description</th>
     *              <th>Comparison rule</th>
     *         </tr>
     *      </thead>
     *      <tbody>
     *          <tr>
     *              <td>EvElemStart</td>
     *              <td>Tag opened</td>
     *              <td>Tag label</td>
     *          </tr>
     *          <tr>
     *              <td>EvElemEnd</td>
     *              <td>Tag closed</td>
     *              <td>Tag label</td>
     *          </tr>
     *          <tr>
     *              <td>EvText</td>
     *              <td>Text node</td>
     *              <td>Node text contents</td>
     *          </tr>
     *          <tr>
     *              <td>EvEntityRef</td>
     *              <td>XML entity (special chars)</td>
     *              <td>Text value</td>
     *          </tr>
     *          <tr>
     *              <td>EvProcInstr</td>
     *              <td>Processing instruction</td>
     *              <td>Instruction target</td>
     *          </tr>
     *          <tr>
     *              <td>EvComment</td>
     *              <td>XML comment</td>
     *              <td>Text contents</td>
     *          </tr>
     *     </tbody>
     * </table>
     *
     * @param evt1 First event
     * @param evt2 Second event
     * @return True if the events are considered equal
     */
    def nameComparison(evt1: XMLEvent, evt2: XMLEvent): Boolean = {
        (evt1, evt2) match {
            case (EvElemStart(_, label1, _, _), EvElemStart(_, label2, _, _)) =>
                label1 equals label2
            case (EvElemEnd(_, label1), EvElemEnd(_, label2)) =>
                label1 equals label2
            case (EvText(a), EvText(b)) =>
                a equals b
            case (EvEntityRef(a), EvEntityRef(b)) =>
                a equals b
            case (EvProcInstr(target1, text1), EvProcInstr(target2, text2)) =>
                target1 equals target2
            case (EvComment(t1), EvComment(t2)) =>
                t1 equals t2
            case _ => false
        }
    }

    /////////////////
    //   ACTIONS   //
    /////////////////

    // FOUND //
    /**
     * Action to pass to the [[XtendedXmlParser.goTo]] method.
     *
     * Returns the next matching element or None if no matching element was found.
     */
    def returnFoundAction[E <: XMLEvent](eltOpt : Option[XMLEvent]) : Option[E] = { eltOpt.map{ elt => elt.asInstanceOf[E]} }

    /**
     * Action to pass to the [[XtendedXmlParser.goTo]] method.
     *
     * Returns a string representation of the next matching element or None if no matching element was found.
     */
    def toStringFoundAction(elt : Option[XMLEvent]) = {
        elt.map{ elt => xmlToString(elt) }
    }

    // LOOP //
    private def unitAction : XMLEvent => Unit = _ => ()
}

/**
 * XML pull parser extending the official scala xml parser.
 *
 * @param src XML source
 */
class XtendedXmlParser(src: Source) extends XMLEventReader(src) {

    import org.xml.xtended.core.XtendedXmlParser._

    //Lowered the queue from 1000 to 100
    override val MaxQueueSize = 100

    /** Last element read **/
    var element_read : XMLEvent = null
    override def next() = {
        element_read = super.next()
        element_read
    }

    /**
     * Parses the XML source until a condition is met on a specific event.
     *
     * @param event XML Event to perform the condition checks on.
     * @param comparison Comparison function which will determine when to stop the loop
     * @param found_action Action to perform when the comparison function returns true
     * @param loop_action Action to perform on each xml event pulled by the parser
     * @tparam T Return type
     * @return An Option, Some[T] or None if the comparison function is never positive
     */
    def goTo[T](event        : XMLEvent,
                comparison   : ((XMLEvent, XMLEvent) => Boolean),
                found_action : Option[XMLEvent] => Option[T],
                loop_action  : XMLEvent => _ )  : Option[T] = {

        def inner_loop(thrown : XMLEvent) : Option[T] = {
            println(thrown)
            if(comparison(event, thrown))
                found_action(Some(thrown))
            else {
                if(!hasNext){
                    found_action(None)
                } else {
                    loop_action(thrown)
                    inner_loop(next())
                }
            }
        }
        inner_loop(next())

    }

    /**
     * Parses the XML source until XML Event specified is found and returns it.
     *
     * Uses [[XtendedXmlParser.returnFoundAction]] as the comparison function.
     *
     * @param tagEvt XML Event to find
     * @tparam T XML Event type
     * @return Some[T] if the event was matched, None otherwise.
     */
    def goToByName[T <: XMLEvent](implicit tagEvt: T) : Option[T] = {
        goTo[T](
            tagEvt,
            nameComparison,
            returnFoundAction[T],
            unitAction
        )
    }

    /**
     * Reads and returns the contents of the next tag specified by the tagStart parameter as a String object.
     *
     * @param tagStart Xml event object containing the tag details
     * @return The tag contents (from the opening to the closing of the tag) or an empty string if the tag was not found.
     */
    def getTagContents(tagStart : EvElemStart) : String = {
        val builder : StringBuilder = new StringBuilder(xmlToString(element_read))
        builder append goTo[String](
            new TagClosed(tagStart.label),
            nameComparison,
            toStringFoundAction,
            (x : XMLEvent) => builder append xmlToString(x)
        ).getOrElse("")
        builder toString()
    }

    /**
     * Reads and returns the contents of the next tag specified by the tagName parameter as an Option[String] object.
     *
     * @param tagName Label of the tag
     * @return The tag contents (from the opening to the closing of the tag) or None if the tag was not found
     */
    def readNextTag(tagName : String) : Option[String] = {
        val tag = new TagOpened(tagName)
        goToByName[EvElemStart](tag).map{ tag => getTagContents(tag)}
    }

}
