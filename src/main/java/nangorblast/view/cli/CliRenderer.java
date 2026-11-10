package nangorblast.view.cli;

import java.io.PrintWriter;
import nangorblast.view.Renderer;
import nangorblast.view.WorldSnapshot;
import nangorblast.view.cli.CliFrame.Cell;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp.Capability;

/**
 * Draws snapshots to a JLine terminal. Only cells that differ from the last frame are
 * rewritten, so the screen never flashes. A resize clears and redraws everything, and a
 * terminal smaller than the frame shows a message asking to enlarge it.
 *
 * <p>Called only from the render thread. Raw mode, the alternate screen, and the cursor
 * are set up by whoever opens the terminal.
 */
public final class CliRenderer implements Renderer {

    private final Terminal terminal;

    // Render thread only.
    private CliFrame shown;
    private Size shownSize;

    public CliRenderer(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void render(WorldSnapshot snapshot) {
        CliFrame frame = CliFrame.compose(snapshot);
        Size size = terminal.getSize();
        if (size.getColumns() < frame.cols() || size.getRows() < frame.rows()) {
            showTooSmall(size, frame);
            return;
        }
        if (shown == null || !size.equals(shownSize)
                || shown.rows() != frame.rows() || shown.cols() != frame.cols()) {
            terminal.puts(Capability.clear_screen);
            shown = null;
        }
        PrintWriter out = terminal.writer();
        for (int row = 0; row < frame.rows(); row++) {
            int col = 0;
            while (col < frame.cols()) {
                if (!changed(frame, row, col)) {
                    col++;
                    continue;
                }
                int start = col;
                AttributedStringBuilder run = new AttributedStringBuilder();
                while (col < frame.cols() && changed(frame, row, col)) {
                    Cell cell = frame.cell(row, col);
                    run.append(String.valueOf(cell.ch()), style(cell));
                    col++;
                }
                terminal.puts(Capability.cursor_address, row, start);
                out.write(run.toAnsi(terminal));
            }
        }
        out.flush();
        shown = frame;
        shownSize = size;
    }

    private boolean changed(CliFrame frame, int row, int col) {
        return shown == null || !shown.cell(row, col).equals(frame.cell(row, col));
    }

    private void showTooSmall(Size size, CliFrame frame) {
        if (shown == null && size.equals(shownSize)) {
            return; // the message for this size is already on screen
        }
        terminal.puts(Capability.clear_screen);
        String message = "Enlarge the terminal to at least " + frame.cols() + " x " + frame.rows()
                + " (now " + size.getColumns() + " x " + size.getRows() + ")";
        terminal.writer().write(new AttributedString(message,
                AttributedStyle.DEFAULT.foregroundRgb(CliFrame.ERROR)).toAnsi(terminal));
        terminal.writer().flush();
        shown = null;
        shownSize = size;
    }

    private static AttributedStyle style(Cell cell) {
        return AttributedStyle.DEFAULT.foregroundRgb(cell.fg()).backgroundRgb(CliFrame.BACKGROUND);
    }
}
