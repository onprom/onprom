package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.State;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.panel.UMLDiagramPanel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by T. E. Kalayci on 17-Nov-2017.
 */
public class DrawingUtility {
    private static final Logger logger = LoggerFactory.getLogger(DrawingUtility.class.getName());
    //variables
    public static final int GAP = 5;
    public static final int GRID_SIZE = 25;
    public static final float ANCHOR_RADIUS = 12;
    public static final float HALF_ANCHOR_RADIUS = ANCHOR_RADIUS / 2;
    //fonts
    public static final Font CLASS_NAME_FONT = new Font(Font.DIALOG, Font.BOLD, 15);
    public static final Font RELATION_FONT = new Font(Font.DIALOG, Font.ITALIC, 12);
    public static final Font ATTRIBUTE_NAME_FONT = new Font(Font.DIALOG, Font.PLAIN, 13);
    //colors
    public static final Color BACKGROUND = new Color(240, 248, 250);
    //contants
    //45 degree
    public static final double D45 = Math.PI / 4;
    //135 degree
    public static final double D135 = 3 * D45;
    //strokes
    private static final float NORMAL_SIZE = 2f;
    public static final Stroke RELATION_STROKE = new BasicStroke(NORMAL_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static final Stroke DISJOINT_STROKE = new BasicStroke(NORMAL_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{10}, 0);
    public static final Stroke NORMAL_STROKE = new BasicStroke(NORMAL_SIZE);
    private static BufferedImage LOGO;

    public static void relayout(@Nonnull Iterable<UMLClass> classes, Graphics g2d) {
        if (UIUtility.confirm(UMLEditorMessages.LAYOUT_DIAGRAM)) {
            try {
                final int columnCount = Integer.parseInt(UIUtility.input("Enter number of columns for grid layout", "5"));
                final int padding = 20;
                int x = padding;
                int y = padding;
                int i = 0;
                int bestY = 0;
                for (UMLClass cls : classes) {
                    cls.setStartX(x);
                    cls.setStartY(y);
                    cls.calculateEndCoordinates(g2d);
                    x = cls.getEndX() + padding;
                    if (bestY < cls.getEndY()) {
                        bestY = cls.getEndY();
                    }
                    i++;
                    if (i % columnCount == 0) {
                        y = y + padding;
                        x = padding;
                        bestY = y;
                    }
                }
            } catch (NumberFormatException e) {
                UIUtility.error("You didn't enter a correct integer number. Please try again.");
            }
        }
    }

    public static void drawGrid(Graphics2D g2d, Dimension size) {
        Color oldColor = g2d.getColor();
        g2d.setColor(new Color(0, 0, 0, 0.1f));
        for (int i = 0; i < size.width; i += GRID_SIZE) {
            g2d.drawLine(i, 0, i, size.height);
            g2d.drawLine(0, i, size.width, i);
        }
        g2d.setColor(oldColor);
    }

    public static void drawLogo(Graphics2D g2d, JViewport viewport) {
        if (viewport != null) {
            Rectangle viewportRectangle = viewport.getViewRect();
            BufferedImage logo = getLogo();
            if (logo != null) {
                Composite oldComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
                g2d.drawImage(logo, (int) viewportRectangle.getX() + 150, (int)
                        viewportRectangle.getY() + 300, logo.getWidth(), logo.getHeight(), null);
                g2d.setComposite(oldComposite);
            }
        }
    }

    private static BufferedImage getLogo() {
        if (LOGO == null) {
            try {
                LOGO = ImageIO.read(IOUtility.getImageURL("onprom"));
            } catch (IOException e) {
                logger.error("Couldn't load logo:" + e.getMessage(), e);
            }
        }
        return LOGO;
    }

    public static void drawSelectionRectangle(Graphics2D g2d, Rectangle selectionArea) {
        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();
        g2d.setStroke(DISJOINT_STROKE);
        g2d.setColor(State.SELECTED.getColor());
        g2d.draw(selectionArea);
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    private static SVGGraphics2D getSVGGraphics(Dimension dimension) {
        SVGGraphics2D svgGenerator = new SVGGraphics2D(GenericDOMImplementation.getDOMImplementation().createDocument("http://www.w3.org/2000/svg", "svg", null));
        //set drawing canvas as the drawing area
        svgGenerator.setSVGCanvasSize(dimension);
        svgGenerator.setClip(0, 0, dimension.width, dimension.height);
        return svgGenerator;
    }

    private static Rectangle getDrawingArea(Collection<DiagramShape> shapes) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        for (DiagramShape shape : shapes) {
            if (shape.getStartX() < minX) {
                minX = shape.getStartX();
            }
            if (shape.getStartY() < minY) {
                minY = shape.getStartY();
            }
            if (shape.getEndX() > maxX) {
                maxX = shape.getEndX();
            }
            if (shape.getEndY() > maxY) {
                maxY = shape.getEndY();
            }
        }
        return new Rectangle(minX - DrawingUtility.GAP, minY - DrawingUtility.GAP, maxX - minX + 2 * DrawingUtility.GAP, maxY - minY + 2 * DrawingUtility.GAP);
    }

    public static void exportImage(UMLDiagramPanel diagramPanel) {
        try {
            File selectedFile = UIUtility.selectFileToSave(FileType.IMAGE);
            if (selectedFile != null) {
                String extension = IOUtility.getFileExtension(selectedFile);
                Rectangle drawingArea = getDrawingArea(diagramPanel.getShapesAndAnchors());
                if (extension.equals("svg")) {
                    SVGGraphics2D svgGenerator = getSVGGraphics(drawingArea.getSize());
                    diagramPanel.paintDiagram(svgGenerator, drawingArea.x, drawingArea.y);
                    svgGenerator.stream(new FileWriter(selectedFile));
                    svgGenerator.dispose();
                } else {
                    BufferedImage bi = new BufferedImage(drawingArea.width, drawingArea.height, BufferedImage.TYPE_INT_RGB);
                    Graphics g = bi.createGraphics();
                    diagramPanel.paintDiagram(g, drawingArea.x, drawingArea.y);
                    ImageIO.write(bi, extension, selectedFile);
                    g.dispose();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            UIUtility.error(e.getMessage());
        }
    }

    public static void print(UMLDiagramPanel diagramPanel) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable((g, format, page) -> {
            if (page > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            // get the bounds of the component
            Rectangle drawingArea = getDrawingArea(diagramPanel.getShapesAndAnchors());
            double cHeight = drawingArea.getSize().getHeight();
            double cWidth = drawingArea.getSize().getWidth();
            // get the bounds of the printable area
            double pHeight = format.getImageableHeight();
            double pWidth = format.getImageableWidth();
            double pXStart = format.getImageableX();
            double pYStart = format.getImageableY();
            //find ratio
            double xRatio = pWidth / cWidth;
            double yRatio = pHeight / cHeight;
            Graphics2D g2d = (Graphics2D) g;
            //translate and scale accordingly
            g2d.translate(pXStart, pYStart);
            g2d.scale(xRatio, yRatio);
            diagramPanel.paintDiagram(g2d, drawingArea.x, drawingArea.y);
            return Printable.PAGE_EXISTS;
        });
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (PrinterException e) {
                UIUtility.error(e.getMessage());
            }
        }
    }


}
