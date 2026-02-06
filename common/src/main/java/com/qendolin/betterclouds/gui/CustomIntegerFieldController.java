package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.number.IntegerFieldController;
import net.minecraft.text.Text;

import java.util.function.Function;

public class CustomIntegerFieldController extends IntegerFieldController {
    public CustomIntegerFieldController(Option<Integer> option, int min, int max, Function<Integer, Text> formatter) {
        super(option, min, max, formatter);
    }

    public CustomIntegerFieldController(Option<Integer> option, int min, int max) {
        super(option, min, max);
    }

    public CustomIntegerFieldController(Option<Integer> option, Function<Integer, Text> formatter) {
        super(option, formatter);
    }

    public CustomIntegerFieldController(Option<Integer> option) {
        super(option);
    }

    @Override
    public boolean isInputValid(String input) {
        return input.matches("-?\\d+|-|");
    }
}
