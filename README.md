# orx-ui

An **unofficial** alternative to `orx-panel` for building UIs in a more immediate-mode style.
Aimed to make quick UIs, but extensible to more complex interfaces.

___

Roadmap (highest priority first):

- [x] Text support
- [x] Stylesheets
- [x] orx-parameters binding
- [x] Other controls (Nodes/contour control, Color Widget, Spring control, etc...)
- [x] friction-based precision
- [ ] Collapsible Widgets // only widgets are collapsible. when collapsed, they should look at their sibling widgets, elements and rearrange
- [ ] Documentation
- [ ] tests
- [ ] Windowed UI
- [ ] GUI interoperability

----
## Usage

run `extend(UI())` at the start of the program, or before declaring any ui element.<br>
The UI makes available Widgets and UIElements.<br>

#### UIElement ####
An interactable UI primitive. You can create your own by extending UIElement
Default basic elements are provided, as well as helper functions to spawn them
(`Button`, `Checkbox`, `Dropdown`, `Slider`, `RangeSlider`, `XYControl`, `ScaleControl`,
`RotateControl`, `TextInput`, `TitleBar`, `Node`, `Handle`)

#### Widget ####
A container of elements and/or widgets. They are not interactable.
they take care of clipping, scroll and layouting.
Basic widgets and helper functions are provided (`ContourWidget`, `TitleBarWidget`,
`TransformWidget`, `VectorWidget`)

----

## Elements

<table>
<tr>
<th > Code </th>
<th> Result </th>
</tr>
<tr>
<td>

#### Button

```kotlin
button(drawer.bounds.offsetEdges(-10.0)) {
    action = { println("clicked") }
}
```
</td>
<td>



</td>
</tr>
<tr>
<td>

#### Checkbox

You can do checkboxes in two ways:


As an element:
```kotlin
val states = (0..3).map {
    object { var on = false }
}

for ((i, state) in states.withIndex()) {
    val r = Rectangle(i * 120.0, 0.0, 120.0).offsetEdges(-10.0)
    checkbox(r, state::on)
}
```

or you can attaching checkbox behavior to any other element
that mutates its `isSelected` property
```kotlin
states.mapIndexed { i, state ->
    val r = Rectangle(i * 120.0, 0.0, 120.0).offsetEdges(-10.0)
    button(r, state::on)
}.asCheckbox()
```
or
```kotlin
asCheckbox {
    states.mapIndexed { i, state ->
        val r = Rectangle(i * 120.0, 0.0, 120.0).offsetEdges(-10.0)
        button(r, state::on)
    }
}
```

tuutyfuyguy
</td>
<td>


</td>
</tr>

<tr>
<td>

#### Slider

```kotlin
var state = object {
    var d = 0.0
    var i = 5
}

slider(rect, 0.0..10.0, state::d)
slider(rect, 0..10, state::i)
```

```kotlin
val state = object {
    var left = 3.0
    var right = 6.0
    var range: ClosedRange<Double> = 2.0..8.0
    var irange: ClosedRange<Int> = 0..5
}

rangeSlider(rect, state::left, state::right, 0.0..12.0)
rangeSlider(rect, state::range, 0.0..12.0)
rangeSlider(rect, state::irange, 0..12)
```
</td>
<td>


</td>
</tr>

<tr>
<td>

#### Dropdown

```kotlin
enum class Test {
    OPTION_0, OPTION_1, OPTION_2, OPTION_3
}

val state = object {
    var current = Test.OPTION_0
}

dropdown<Test>(grid[1], state::current)
```

```kotlin
val state = object {
    var list = selectableListOf("A", "B", "C")
    var mlist = selectableMutableListOf(1, 2, 3)
}

dropdown(drawer.bounds.offsetEdges(-10.0).copy(height = 40.0), state::list)
dropdown(drawer.bounds.offsetEdges(-10.0).copy(height = 40.0), state::mlist)
```

</td>
<td>


</td>
</tr>

<tr>
<td>

#### Text

```kotlin
val state = object {
    var text = "Hi! I am a single-line text. Cut me, scroll me, paste me, replace me."
    
    var text2 = "This is a multi-line text-box. I am resizable (drag bottom corner)." +
            "You can use me like any traditional textbox. Double click on words to select them," +
            "SHIFT+arrows to select characters, CTRL+A to select all, CTRL+C to copy" +
            "CTRL+X to cut, CTRL+V to paste. Scroll with mouse and arrow buttons."
}

textInput(Vector2(10.0), width - 20.0, state::text)
textboxInput(Rectangle(10.0, 70.0, width - 20.0, 150.0), state::text2)
```

</td>
<td>
</td>
</tr>
</table>

----

## Widgets

<table>
<th>Code</th>
<th>Demo</th>

<tr>
<td>

#### TransformWidget

```kotlin
val state = object {
    var m = Matrix44.IDENTITY
}

transformWidget(r[0], state::m,
    Vector2(-5.0)..Vector2(5.0),
    Vector2.ONE..Vector2(5.0)
)
```
</td>
<td>
</td>
</tr>

<tr>
<td>

#### VectorWidget

```kotlin
var state = object {
    var v2 = Vector2(0.5)
    var v3 = Vector3(0.5)
    var v4 = Vector4(0.5)
}

vectorWidget(drawer.bounds.offsetEdges(-10.0), 0.0..10.0, state::v2)
vectorWidget(drawer.bounds.offsetEdges(-10.0), 0.0..10.0, state::v3)
vectorWidget(drawer.bounds.offsetEdges(-10.0), 0.0..10.0, state::v4)
```
</td>
<td>
</td>
</tr>


<tr>
<td>

#### ContourWidget

```kotlin
contourWidget(state::contour)
```
</td>
<td>
</td>
</tr>


</table>

----

## Layouting

#### Grid

Various helpers are provided to build layouts based on OPENRDR RectangleGrid.

```kotlin
widget(rect) {
    grid(2, 2) { r -> button(r) }
}
```

```kotlin
widget(rect) {
    gridIndexed(2, 2) { i, r ->
        button(r) { label = "$i" }
    }
}
```

```kotlin
widget(rect) {
    gridSlots(1, 2, gutterX = 5.0, gutterY = 5.0) { slots ->
        button(slots[0], configure)
        button(slots[1], configure)
    }
}
```

```kotlin
widget(rect) {
    irregularGrid(listOf(0.2, 0.5, 0.3), listOf(0.1, 0.4, 0.5)) { rect ->
        button(rect.offsetEdges(-5.0))
    }
}
```
----

## Styling

Elements and widgets can be styled in different ways.

You can change the style of a Widget or UIElement directly

```kotlin
button(rect) {
    style.backgroundColor = ColorRGBa.GRAY
    style.borderRadius = 20.0
}
```

Or you can build stylesheet that make use of Widgets' and UIElements' "selectors" (not dissimilar from css classes).
<br>Here's an example of a stylesheet:

```kotlin
widget(drawer.bounds.offsetEdges(-10.0)) {
    gridSlots(2, 2, gutterX = 10.0, gutterY = 10.0) {
        button(it[0]) {
            selectors = setOf("orange", "nf")
        }
        button(it[1]) {
            selectors = setOf("orange", "round", "nf")
        }
        button(it[2]) {
            selectors = setOf("big-label", "nf")
        }
        button(it[3]) {
            selectors = setOf("big-label", "round", "nf")
        }
    }
}

stylesheet {
    "nf" {
        hoverColor = null
    }

    "orange" {
        backgroundColor = ColorRGBa.ORANGE_RED
    }

    "round" {
        borderRadius = 300.0
    }

    "big-label" {
        transition(::fontSize, 0.5, Easing.SineInOut)
    }

    "big-label"(hover) {
        fontSize = 30.0
    }
}
```
<br/>

Hover and focus states are handled. They can be referenced in different ways
```kotlin
"big-label:hover" {
    fontSize = 30.0
}
```
```kotlin
"big-label"(StyleModifier.HOVER) {
    fontSize = 30.0
}
```
```kotlin
"big-label"(hover) {
    fontSize = 30.0
}
```

<br>

Stylesheets can be saved and loaded from HOCON files.

```kotlin
style.toFile(File("data/style/demo-stylesheet-01.conf"))
ui.loadStylesheet(File("data/style/demo-stylesheet-01.conf"))
```

<br>
CSS-style transitions are supported.
Not specifying a target property will animate every changing double-based and color-based property.

```kotlin
"round" {
    hoverColor = null
    transition(0.5, Easing.SineInOut)
}

"round:hover" {
    backgroundColor = ColorRGBa.ORANGE_RED
    borderRadius = 200.0
}
```

Alternatively, you can target specific properties
```kotlin
"round" { 
    fontSize = 20.0
    transition(::fontSize, 0.5, Easing.SineInOut) 
}

"round:hover" {
    fontSize = 40.0
}
```
P.S.: font-size is optimized to .1 precision.

----

## Animation

An internal Animatable-based animation mechanism is made available.

```kotlin
class SpawnAnimation: UIAnimatable() {
    var t = 0.01

    fun fadeIn() {
        t = 0.01
        ::t.animate(1.0, 500, Easing.CubicInOut)
    }

    context(element: UIElement)
    override fun update() {
        element.bounds = element.boundsCopy.scaledBy(t)
    }
}

button(Rectangle.fromCenter(drawer.bounds.center, 50.0)) {
    label = ""
    animations = listOf(SpawnAnimation().apply { fadeIn() })
    action = { (animations[0] as SpawnAnimation).fadeIn() }
}
```

----

## GUI

UI provides GUI-like functionality. Parameters are serialized and deserialized in hocon.

```kotlin
val settings = object {
    @Vector2Parameter("v2", -2.0, 2.0)
    var v2 = Vector2.ZERO

    @Vector3Parameter("v3", -2.0, 2.0)
    var v3 = Vector3.ZERO

    @Vector4Parameter("v4", -2.0, 2.0)
    var v4 = Vector4.ZERO
}

extend(UI()).fromParameters(settings)
```

Auto-save is enabled by default.

<br>

#### Precision

UIElements have a precision property. For drag-based elements like sliders,
increasing this value (default=1) will add friction to the drag action.

```kotlin
slider(it[1], 0.0..5.0, state::x1) { precision = 2 }
```

----

<br>
UI provides more functionality and ways to do things than shown here. Feel free to check out the demos for this.