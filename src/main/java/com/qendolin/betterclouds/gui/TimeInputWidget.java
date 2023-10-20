package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.gui.color.GammaRgbColor;
import com.qendolin.betterclouds.gui.color.IColor;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;
import java.util.function.Consumer;

public class TimeInputWidget extends TextFieldWidget {
    public Consumer<Integer> onChange = null;
    protected String prevChangeValue;

    public TimeInputWidget(Bounds bounds) {
        super(MinecraftClient.getInstance().textRenderer, bounds.x(), bounds.y(), bounds.width(), bounds.height(), Text.empty());
        setTextPredicate(TimeInputWidget::isValidTime);
        setChangedListener(this::onChange);
        setRenderTextProvider(this::renderText);
        setDrawsBackground(false);
        setMaxLength(4);
        setText("0000");
    }

    private static boolean isValidTime(String s) {
        if(s.length() != 4) return false;
        try {
            int h = Integer.parseInt(s.substring(0, 2));
            int m = Integer.parseInt(s.substring(2, 4));
            return h >= 0 && h < 24 && m >= 0 && m < 95;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public void setTime(int time) {
        setText(timeToHours(time));
    }

    protected static String timeToHours(int time) {
        // shift phase to midnight
        time = ((time + 6000) % 24000 + 24000) % 24000;
        float f = time / 24000f;
        int seconds = MathHelper.floor(60 * 60 * 24 * f);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        return String.format("%02d%02d", hours, minutes % 60);
    }

    protected static int hoursToTime(String s) {
        String hours, minutes;
        if(s.length() == 5) {
            hours = s.substring(0, 2);
            minutes = s.substring(3, 5);
        } else if(s.length() == 4) {
            hours = s.substring(0, 2);
            minutes = s.substring(2, 4);
        } else {
            return -1;
        }
        try {
            int h = Integer.parseInt(hours);
            int m = Integer.parseInt(minutes);

            if(h == 24 && m == 0) h = 0;

            if(h < 0 || h >= 24 || m < 0 || m >= 60) return -1;

            int seconds = h * 3600 + m * 60;
            float f = seconds / (60 * 60 * 24f);
            int time = MathHelper.floor(f * 24000);
            // shift phase to sunrise
            time = ((time - 6000) % 24000 + 24000) % 24000;
            return time;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    @Override
    public void write(String text) {
        int colon = text.indexOf(':');
        if(colon != -1) {
            text = text.substring(0, colon) + text.substring(colon+1);
        }

        int start = Math.min(this.selectionStart, this.selectionEnd);
        int end = Math.max(this.selectionStart, this.selectionEnd);
        int tooMuch = Math.max(0, text.length() - (end-start));
        int tooLittle = Math.max(0, (end-start) -  text.length());
        String filler = "0".repeat(tooLittle);
        String string = new StringBuilder(getText()).replace(start, end, filler+text).delete(0, tooMuch).toString();
        setText(string);
        setCursor(end);
        setSelectionEnd(end);
    }

    public void eraseCharacters(int offset) {
        if (getText().isEmpty()) {
            return;
        }
        if (selectionEnd != selectionStart) {
            write("0000");
            return;
        }
        int want = Util.moveCursor(getText(), selectionStart, offset);
        int start = Math.min(want, selectionStart);
        int end = Math.max(want, selectionStart);
        if (start == end) {
            return;
        }
        String filler = "0".repeat(end-start);
        String string = new StringBuilder(filler).append(getText()).delete(filler.length() + start, filler.length() + end).toString();
        setText(string);
        setCursor(end);
        setSelectionEnd(end);
    }

    protected OrderedText renderText(String s, int firstChar) {
        if(firstChar == 2) return Text.literal(":").append(s).asOrderedText();
        if(firstChar == 3) return Text.literal(s).asOrderedText();
        if(firstChar == 0 && s.length() <= 1) return Text.literal(s).asOrderedText();
        MutableText text = Text.literal(s.substring(0, 2-firstChar));
        if(s.length() - (2-firstChar) > 0) {
            text = text.append(":").append(s.substring(2-firstChar));
        }
        return text.asOrderedText();
    }

    private void onChange(String s) {
        if(Objects.equals(s, prevChangeValue)) return;
        prevChangeValue = s;
        int time = hoursToTime(s);
        if(time != -1 && onChange != null) {
            onChange.accept(time);
        }
    }
}
