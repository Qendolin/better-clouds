package com.qendolin.betterclouds.clouds;

import java.nio.FloatBuffer;

public class Sort {
    public static void insertionSort(List list) {
        for (int i = 1; i < list.size(); i++) {
            int j = i - 1;
            while (j >= 0 && list.compare(j, j + 1) > 0) {
                list.swap(j, j + 1);
                j--;
            }
        }
    }

    public static void insertionSortPartial(List list, int start, int count) {
        for (int i = start; i < Math.min(list.size(), start + count); i++) {
            int j = i - 1;
            while (j >= 0 && list.compare(j, j + 1) > 0) {
                list.swap(j, j + 1);
                j--;
            }
        }
    }

    public static void quickSort(List list) {
        dualPivotQuickSort(list, 0, list.size() - 1);
    }

//    public static void quickSort(List list) {
//        quickSort(list, 0, list.size() - 1);
//    }
//
//    private static void quickSort(List list, int low, int high) {
//        if (low < high) {
//            int index = partition(list, low, high);
//
//            quickSort(list, low, index - 1);
//            quickSort(list, index + 1, high);
//        }
//    }
//
//    private static int partition(List list, int low, int high) {
//        int pivot = high;
//        int i = (low - 1);
//
//        for (int j = low; j <= high - 1; j++) {
//            if(list.compare(j, pivot) < 0) {
//                i++;
//                list.swap(i, j);
//            }
//        }
//        list.swap(i + 1, high);
//        return (i + 1);
//    }

    private static void dualPivotQuickSort(List list, int low, int high) {
        if (low < high) {

            // piv[] stores left pivot and right pivot.
            // piv[0] means left pivot and
            // piv[1] means right pivot
            int[] piv;
            piv = partition(list, low, high);

            dualPivotQuickSort(list, low, piv[0] - 1);
            dualPivotQuickSort(list, piv[0] + 1, piv[1] - 1);
            dualPivotQuickSort(list, piv[1] + 1, high);
        }
    }

    private static int[] partition(List list, int low, int high) {
        if (list.compare(low, high) > 0)
            list.swap(low, high);

        // p is the left pivot, and q
        // is the right pivot.
        int j = low + 1;
        int g = high - 1, k = low + 1;
        int p = low, q = high;

        while (k <= g) {

            // If elements are less than the left pivot
            if (list.compare(k, p) < 0) {
                list.swap(k, j);
                j++;
            }

            // If elements are greater than or equal
            // to the right pivot
            else if (list.compare(k, q) >= 0) {

                while (list.compare(g, q) > 0 && k < g)
                    g--;

                list.swap(k, g);
                g--;

                if (list.compare(k, q) < 0) {
                    list.swap(k, j);
                    j++;
                }
            }
            k++;
        }
        j--;
        g++;

        // Bring pivots to their appropriate positions.
        list.swap(low, j);
        list.swap(high, g);

        // Returning the indices of the pivots
        // because we cannot return two elements
        // from a function, we do that using an array.
        return new int[]{j, g};
    }

    public interface List {

        /**
         * Swaps the objects at the specified indices
         *
         * @param i The index of the primary object
         * @param j The index of the secondary object
         */
        void swap(int i, int j);

        /**
         * @param i The index of the primary object
         * @param j The index of the secondary object
         * @return >0 if the primary is greater than the secondary, <0 if the primary is less than the secondary, 0 if they are equal
         */
        int compare(int i, int j);

        /**
         * @return The size of the list
         */
        int size();
    }

}
