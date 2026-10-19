package elements

import org.openrndr.panel.collections.SelectableList
import org.openrndr.panel.collections.SelectableMutableList

/** function wrappers for SelectableList and SelectableMutableList.
 *  For non-empty lists, selected index defaults to 0 */

fun <E> selectableListOf(): SelectableList<E> = SelectableList(listOf())
fun <E> selectableListOf(vararg elements: E): SelectableList<E> {
    return if (elements.isNotEmpty()) SelectableList(elements.asList(), 0) else selectableListOf()
}
fun <E> selectableMutableListOf(): SelectableMutableList<E> = SelectableMutableList(listOf())
fun <E> selectableMutableListOf(vararg elements: E): SelectableMutableList<E> {
    return if (elements.isNotEmpty()) SelectableMutableList(elements.asList().toMutableList(), 0) else selectableMutableListOf()
}