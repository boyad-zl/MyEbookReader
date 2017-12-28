package com.example.epubreader.view.book;

/**
 * Created by Boyad on 2017/12/20.
 */

public interface BookViewEnums {


    public enum Direction {
        leftToRight(true), rightToLeft(true), up(false), down(false);

        public final boolean isHorizontal;

        Direction(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
        }
    }

    public enum Animation{
        none, curl, slide, slideOldStyle, shift
    }
}
