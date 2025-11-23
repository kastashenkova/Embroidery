# Ukrainian Embroidery Pattern Designer

An interactive Java Swing application for creating and editing traditional Ukrainian folk embroidery patterns with built-in symmetry tools and the "Kateryna" ornament template.

## Features

- **Interactive Drawing**: Click and drag to create embroidery patterns
- **Color Palette**: Red, black, and white ornament colors
- **Symmetry Modes**: Draw with horizontal, vertical, or quadrant symmetry
- **Pattern Duplication**: Mirror and repeat pattern fragments
- **Pre-built Template**: "Kateryna" traditional Ukrainian ornament
- **File Operations**: Save and open patterns as PNG images
- **Dynamic Canvas**: Auto-adjusts to window size
- **Grid System**: Numbered rows and columns for precision

## Color System

The application uses a three-color palette typical of Ukrainian embroidery:

- **Red** (200, 50, 50): Primary ornament color
- **Black** (50, 50, 50): Accent and outline color
- **White** (255, 255, 255): Background and ornament highlights

Pattern matrix values:
- `0` = Background (transparent)
- `1` = Red
- `2` = Black  
- `3` = White (ornament part)

## Usage

### Running the Application

```bash
javac org/example/EmbroideryPattern.java
java org.example.EmbroideryPattern
```

### Controls

**Action Buttons:**
- **Kateryna Pattern**: Generate the traditional "Kateryna" ornament
- **Clear**: Erase entire canvas
- **Save PNG**: Export pattern as PNG image
- **Open PNG**: Import pattern from PNG file

**Color Selection:**
- **Red**: Set drawing color to red
- **Black**: Set drawing color to black
- **White (Ornament)**: Set drawing color to white
- **Eraser**: Switch to erase mode
- **Draw Mode**: Switch back to drawing mode

**Drawing Symmetry Modes:**
- **NONE**: No symmetry, free drawing
- **HORIZONTAL**: Mirror horizontally around center
- **VERTICAL**: Mirror vertically around center
- **QUADRANT**: Four-way symmetry (both axes)

### Advanced Features

#### Fragment Duplication

Duplicate a specific rectangular area with mirroring:

1. Set **Row Start**, **Col Start** coordinates
2. Set **Rows** and **Cols** dimensions
3. Choose duplication symmetry mode
4. Click **Apply Duplication (with flip)**

Example: Duplicate a 31×31 pattern starting at (0,0) with quadrant symmetry.

#### Full Pattern Duplication

Duplicate the entire active pattern in any direction:

- **Duplicate Right**: Mirror pattern horizontally to the right
- **Duplicate Left**: Mirror pattern horizontally to the left
- **Duplicate Up**: Mirror pattern vertically upward
- **Duplicate Down**: Mirror pattern vertically downward

These operations automatically find the pattern boundaries and apply mirroring.

## The "Kateryna" Pattern

The built-in "Kateryna" ornament is a 31×31 pixel traditional Ukrainian embroidery design featuring:

- Central black cross motif
- Symmetrical arms and decorative elements
- White highlights within the ornament
- Random red accents (20% probability)

The pattern is automatically centered on the canvas and includes perfect four-way symmetry.

## Technical Details

### Grid System

- **Default Cell Size**: 10×10 pixels
- **Initial Grid**: 61×61 cells
- **Dynamic Resizing**: Automatically expands as needed
- **Header Offsets**: 30px for row/column numbers

### Canvas Layout

```
        [Column Numbers]
[Row    [Main Grid Area]
Numbers]
```

The canvas includes:
- Top margin for column numbers (0, 1, 2, ...)
- Left margin for row numbers
- Main drawing area with visible grid lines

### Symmetry Implementation

**Drawing Symmetry**: Applied in real-time as you draw
- Calculates mirror coordinates relative to canvas center
- Updates all symmetric points simultaneously

**Duplication Symmetry**: Applied to saved fragments
- Horizontal flip: reverses column order
- Vertical flip: reverses row order
- Quadrant flip: applies both transformations

### File Format

Saved PNG files preserve the pattern with:
- Exact cell colors (red, black, white)
- Grid lines for reference
- No headers (pure pattern data)

When loading, the application:
- Samples center of each cell
- Uses color threshold matching (±30 RGB)
- Dynamically resizes canvas to fit pattern

## Architecture

### Key Classes

**EmbroideryPattern** (extends JPanel)
- Main application class and drawing surface
- Handles all user interactions
- Manages pattern state and rendering

### Data Structures

```java
private int[][] pattern;  // Pattern matrix
private int currentPatternWidth;
private int currentPatternHeight;
private Color currentColor;
private SymmetryMode symmetryMode;
```

### Key Methods

**Pattern Generation:**
- `generateKaterynaPattern()`: Creates the Kateryna ornament
- `setPixelKateryna(row, col, value)`: Sets pixel with symmetry

**Drawing Operations:**
- `handleMouseEvent(MouseEvent e)`: Processes mouse input
- `setPixelSymmetrically(row, col, value)`: Applies symmetry

**Duplication:**
- `applyFragmentDuplication(...)`: Duplicates specified area
- `duplicateFullPatternRight/Left/Up/Down()`: Mirrors entire pattern

**File I/O:**
- `savePatternAsPng()`: Exports to PNG
- `openPatternFromPng()`: Imports from PNG

**Utility:**
- `flipFragmentHorizontal/Vertical/Quadrant(...)`: Mirror transformations
- `findActivePatternBounds()`: Detects pattern boundaries

## Example Workflow

### Creating a Traditional Pattern

1. Click **Kateryna Pattern** to load template
2. Select **Red** color
3. Choose **QUADRANT** symmetry mode
4. Draw additional decorative elements
5. Use **Duplicate Right** to extend pattern
6. Click **Save PNG** to export

### Custom Design

1. Click **Clear** to start fresh
2. Select desired color
3. Choose symmetry mode (or NONE for free drawing)
4. Draw your pattern
5. Use fragment duplication to create repetitions
6. Save when complete

## Tips & Best Practices

- **Use symmetry modes** for traditional balanced patterns
- **Save frequently** to preserve your work
- **Start with Kateryna** and modify rather than from scratch
- **Quadrant symmetry** creates the most traditional look
- **Fragment duplication** is great for borders and repetitive elements
- **Grid numbers** help with precise coordinate-based duplication

## Keyboard Shortcuts

None implemented - all operations use mouse and buttons.

## Limitations

- No undo/redo functionality
- Single layer only (no overlapping)
- Fixed cell size (10×10 pixels)
- PNG import requires specific color matching
- No pattern library or templates beyond Kateryna

## Future Enhancements

Potential features to add:
- Multiple pattern templates
- Undo/redo stack
- Color picker for custom colors
- Pattern rotation and scaling
- Export to embroidery formats
- Pattern library/gallery
- Keyboard shortcuts
- Zoom functionality

## Cultural Context

Ukrainian embroidery (вишивка) is a traditional folk art with regional variations. The "Kateryna" pattern represents a common symmetric cross-motif style featuring:

- Central geometric designs
- Floral and geometric accents  
- Traditional color combinations
- Four-way rotational symmetry

This tool helps preserve and create patterns in this cultural tradition.

## Educational Value

Perfect for:
- Learning traditional Ukrainian ornament patterns
- Understanding symmetry in design
- Exploring pixel art techniques
- Cultural heritage preservation
- Pattern mathematics

---

**Author**: Kateryna Astashenkova  
**License**: Educational project - free to use and modify.
