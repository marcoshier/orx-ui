package widgets

import elements.HandleNode
import elements.button
import elements.titleBar
import mousePosition
import org.openrndr.KeyModifier
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment2D
import org.openrndr.shape.ShapeContour
import registerWidget
import ui.UIElement
import ui.close
import widget.Widget
import widget.WidgetBuilder
import widget.addElement
import widget.addElements
import widget.close
import widget.grid
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

class Anchor(
    var p: Vector2,
    var cIn: Vector2? = null,
    var cOut: Vector2? = null,
)

class ContourWidget(
    private val getter: () -> ShapeContour,
    private val setter: (ShapeContour) -> Unit,
    configure: ContourWidget.() -> Unit = {}
) : Widget(getter().bounds.offsetEdges(30.0)) {

    constructor(
        contour: KMutableProperty0<ShapeContour>,
        configure: ContourWidget.() -> Unit = {}
    ) : this({ contour.get() }, { contour.set(it) }, configure)

    var editable = true
    var distanceThreshold = 7.5

    private var contourClosed = getter().closed
    private var anchors = getter().toAnchors()
    private var _contour = getter()

    override val elements = mutableListOf<UIElement>()

    private val selected = mutableSetOf<Int>()
    private val unmirrored = mutableSetOf<Int>()

    private class VertexHandles(
        var index: Int,
        val vertex: HandleNode,
        val cIn: HandleNode?,
        val cOut: HandleNode?
    )

    private val vertexHandles = mutableListOf<VertexHandles>()
    private val vertexCount: Int get() = anchors.size

    private fun commit() {
        _contour = anchors.toContour(contourClosed)
        setter(_contour)
    }

    private fun isSelected(j: Int): Boolean {
        val n = vertexCount
        val k = if (contourClosed) ((j % n) + n) % n else j
        return k in 0 until n && k in selected
    }

    private fun isAnySelected(vararg j: Int): Boolean {
        return j.any { isSelected(it) }
    }

    private fun moveSelectedBy(delta: Vector2) {
        if (selected.isEmpty()) return

        for (i in selected) anchors[i].let {
            it.p += delta
            it.cIn = it.cIn?.plus(delta)
            it.cOut = it.cOut?.plus(delta)
        }

        commit()
    }

    private fun smoothVertex(i: Int) {
        val n = vertexCount
        val hasIn = contourClosed || i > 0
        val hasOut = contourClosed || i < n - 1

        val p = anchors[i].p
        val prev = if (hasIn) anchors[(i - 1 + n) % n].p else p
        val next = if (hasOut) anchors[(i + 1) % n].p else p

        val dir = next - prev
        if (dir.length < 1e-6) return

        val t = dir.normalized

        if (hasIn) {
            anchors[i].cIn = p - t * ((p - prev).length / 3.0)
        }
        if (hasOut) {
            anchors[i].cOut = p + t * ((next - p).length / 3.0)
        }

        rebuildHandles(setOf(i))
        commit()
    }

    private fun removeVertex(i: Int) {
        if (anchors.size <= 2) return

        anchors.removeAt(i)
        rebuildHandles()
        commit()
    }

    private fun insertAt(segIdx: Int, t: Double) {
        val n = anchors.size
        val a = anchors[segIdx]
        val b = anchors[(segIdx + 1) % n]

        val seg = if (a.cOut == null && b.cIn == null) {
            Segment2D(a.p, b.p)
        } else {
            Segment2D(a.p, a.cOut ?: a.p, b.cIn ?: b.p, b.p)
        }

        if (seg.control.isEmpty()) {
            anchors.add(segIdx + 1, Anchor(seg.position(t)))
        } else {
            val parts = seg.split(t)
            val s0 = parts[0]
            val s1 = parts.getOrElse(1) { parts[0] }

            a.cOut = s0.control.getOrNull(0)
            b.cIn = s1.control.getOrNull(1)

            anchors.add(
                segIdx + 1,
                Anchor(s0.end, s0.control.getOrNull(1), s1.control.getOrNull(0))
            )
        }

        rebuildHandles(setOf(segIdx + 1))
        commit()
    }


    private fun rebuildHandles(selection: Set<Int> = emptySet()) {
        val old = elements.filterIsInstance<HandleNode>()
        old.forEach { it.close() }

        elements.removeAll(old)
        vertexHandles.clear()
        unmirrored.clear()

        selected.clear()
        selected.addAll(
            selection.filter { it in 0 until vertexCount }
        )

        val handles = createHandles()
        addElements(handles)
    }

    private fun createHandles(): List<UIElement> {
        return (0 until vertexCount).flatMap { i ->
            val vertex = vertexHandle(i)
            val inHandle = anchors[i].cIn?.let { controlHandle(i, vertex, directionOut = false) }
            val outHandle = anchors[i].cOut?.let { controlHandle(i, vertex, directionOut = true) }

            vertex.a = inHandle
            vertex.b = outHandle
            vertexHandles += VertexHandles(i, vertex, inHandle, outHandle)

            listOfNotNull(inHandle, outHandle, vertex)
        }
    }

    private fun syncHandles() {
        for (vh in vertexHandles) {
            if (vh.index >= vertexCount) continue

            val a = anchors[vh.index]
            vh.vertex.moveCenterTo(a.p)
            vh.vertex.isSelected = vh.index in selected

            a.cIn?.let { vh.cIn?.moveCenterTo(it) }
            a.cOut?.let { vh.cOut?.moveCenterTo(it) }
        }
    }

    private fun UIElement.moveCenterTo(p: Vector2) {
        bounds = Rectangle.fromCenter(p, bounds.width, bounds.height)
    }

    fun selectAll() {
        for (i in 0 until vertexCount) selected.add(i)
    }

    fun deselectAll() {
        selected.clear()
    }

    private fun vertexHandle(i: Int): HandleNode {
        val pos = anchors[i].p

        return HandleNode(pos) {
            radius = 6.0
            var lastPos = pos
            interactable = { editable }

            onButtonDown = { event ->
                if (KeyModifier.SHIFT in event.modifiers) {
                    if (!selected.remove(i)) {
                        selected.add(i)
                    }
                } else {
                    if (i !in selected) {
                        selected.clear()
                        selected.add(i)
                    }
                }

                lastPos = event.position
            }

            onDrag = { event ->
                if (i !in selected) {
                    selected.clear()
                    selected.add(i)
                }

                moveSelectedBy(event.position - lastPos)
                lastPos = event.position
            }

            onButtonUp = { event ->
                when {
                    KeyModifier.ALT in event.modifiers -> smoothVertex(i)
                    KeyModifier.CTRL in event.modifiers -> removeVertex(i)
                }
            }
        }
    }

    private fun controlHandle(i: Int, vertex: HandleNode, directionOut: Boolean): HandleNode {
        val control = if (directionOut) anchors[i].cOut else anchors[i].cIn
        val startPos = control ?: anchors[i].p
        val handle = HandleNode(startPos, null, vertex) {
            interactable = { editable }
            radius = 6.0
        }

        handle.visible = {
            if (directionOut) {
                isAnySelected(i, i + 1)
            } else {
                isAnySelected(i, i - 1)
            }
        }

        handle.onDrag = { event ->
            if (KeyModifier.ALT in event.modifiers) unmirrored.add(i)
            if (KeyModifier.SHIFT in event.modifiers) unmirrored.remove(i)

            val a = anchors[i]
            val pos = event.position

            if (directionOut) {
                a.cOut = pos
            } else {
                a.cIn = pos
            }

            if (i !in unmirrored) {
                val mirrored = a.p - (pos - a.p)
                if (directionOut) {
                    if (a.cIn != null) {
                        a.cIn = mirrored
                    }
                } else {
                    if (a.cOut != null) {
                        a.cOut = mirrored
                    }
                }
            }

            commit()
        }

        return handle
    }

    inner class ContourElement : UIElement(getter().bounds) {

        fun isNear(contour: ShapeContour, pos: Vector2): Boolean {
            val p = contour.nearest(pos)
            val d = p.position.distanceTo(pos)
            return d < distanceThreshold
        }

        init {
            buttonDown.listen {
                if (!editable) return@listen
                it.cancelPropagation()
            }

            buttonUp.listen {
                if (!editable) return@listen

                isFocused = false
                if (KeyModifier.SHIFT in it.modifiers) {
                    val c = anchors.toContour(contourClosed)
                    if (c !== ShapeContour.EMPTY) {
                        if (isNear(c, it.position)) {
                            val p = c.nearest(it.position)
                            val idx = c.segments.indexOf(p.segment)
                            if (idx != -1) {
                                insertAt(idx, p.segmentT)
                            }
                        }
                    }
                } else {
                    selected.clear()
                }
            }
        }

        override fun draw(drawer: Drawer) {
            val contour = anchors.toContour(contourClosed)
            bounds = contour.let {
                if (it === ShapeContour.EMPTY) {
                    bounds
                } else {
                    it.bounds.offsetEdges(50.0)
                }
            }

            interactable = { editable && isNear(contour, mousePosition()) }
        }
    }

    val contourElement = ContourElement()

    init {
        clip = false
        style.backgroundColor = null
        style.strokeColor = null

        addElement(contourElement)
        addElements(createHandles())
        configure()
    }

    override fun draw(drawer: Drawer, focusElement: UIElement?) {
        val external = getter()
        if (external !== _contour) {
            contourClosed = external.closed
            anchors = external.toAnchors()
            _contour = external
            rebuildHandles()
        }

        syncHandles()
        bounds = contourElement.bounds

        if (editable) {
            super.draw(drawer, focusElement)
        }
    }
}

class ContourWidgetControl(
    bounds: Rectangle,
    widget: ContourWidget,
    draggable: Boolean,
    configure: ContourWidgetControl.() -> Unit = {}
): Widget(bounds) {

    private val my = if (draggable) 20.0 else 5.0
    override val elements = grid(1, 3, 5.0, my, 5.0, 5.0) {
        val rs = it.map {
            val off = if (draggable) 10.0 else 0.0
            it.movedBy(Vector2.UNIT_Y * off)
        }
        if (draggable) {
            titleBar("CONTOUR",
                bounds.copy(height = 20.0),
                widgetTitlebar = true
            )
        }
        button(rs[0]) {
            label = "SELECT ALL"
            action = { widget.selectAll() }
        }
        button(rs[1]) {
            label = "DESELECT ALL"
            action = { widget.deselectAll() }
        }
        button(rs[2]) {
            label = "EDITABLE"
            action = { widget.editable = !widget.editable }
        }
    }

    init {
        style.strokeColor = null
        if (draggable) {
            style.backgroundColor = ColorRGBa.WHITE
        }
        configure()
    }
}

fun ShapeContour.toAnchors(): MutableList<Anchor> {
    if (segments.isEmpty()) return mutableListOf()

    val segs = segments.map { if (it.control.size == 1) it.cubic else it }
    val n = if (closed) segs.size else segs.size + 1

    return MutableList(n) { i ->
        val p = if (i < segs.size) segs[i].start else segs[i - 1].end
        val cOut = if (i < segs.size) segs[i].control.firstOrNull() else null
        val cIn = if (closed || i > 0) segs[(i - 1 + segs.size) % segs.size].control.lastOrNull() else null

        Anchor(p, cIn, cOut)
    }
}

fun List<Anchor>.toContour(closed: Boolean): ShapeContour {
    if (size < 2) return ShapeContour.EMPTY

    val count = if (closed) size else size - 1
    val segs = (0 until count).map { i ->
        val a = this[i]
        val b = this[(i + 1) % size]
        if (a.cOut == null && b.cIn == null) {
            Segment2D(a.p, b.p)
        } else {
            Segment2D(a.p, a.cOut ?: a.p, b.cIn ?: b.p, b.p)
        }
    }

    return ShapeContour.fromSegments(segs, closed)
}


fun contourWidget(
    contour: KMutableProperty0<ShapeContour>,
    showControls: Boolean = true,
    draggableControls: Boolean = true,
    configure: ContourWidget.() -> Unit = {}
): ContourWidget {
    val w = ContourWidget(contour, configure)
    registerWidget(w)

    if (showControls) {
        val c = ContourWidgetControl(Rectangle(10.0, 10.0, 200.0, 170.0), w, draggableControls)
        registerWidget(c)
    }

    return w
}

fun contourWidget(
    obj: Any,
    valueRef: KMutableProperty1<Any, ShapeContour>,
    configure: ContourWidget.() -> Unit = {}
): ContourWidget {
    val w = ContourWidget({ valueRef.get(obj) }, { valueRef.set(obj, it) }, configure)
    val c = ContourWidgetControl(Rectangle(10.0, 10.0, 200.0, 170.0), w, false)
    registerWidget(w)
    registerWidget(c)
    return w
}

fun WidgetBuilder.contourWidget(
    contour: KMutableProperty0<ShapeContour>,
    configure: ContourWidget.() -> Unit = {}
): ContourWidget {
    val w = ContourWidget(contour, configure)
    registerWidget(w)
    this.widget.closed.listen { w.close() }
    return w
}

fun WidgetBuilder.contourWidget(
    obj: Any,
    valueRef: KMutableProperty1<Any, ShapeContour>,
    configure: ContourWidget.() -> Unit = {},
): ContourWidget {
    val w = ContourWidget({ valueRef.get(obj) }, { valueRef.set(obj, it) }, configure)
    registerWidget(w)
    val c = ContourWidgetControl(currentElementBounds.copy(height = 170.0), w, false)
    c.parent = this.widget
    this.widget.widgets.add(c)
    this.widget.closed.listen { w.close() }
    return w
}