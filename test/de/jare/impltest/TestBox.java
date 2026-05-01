/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

import java.util.List;

/**
 * Test container class that holds various value types for testing.
 * Contains fields for a single value, a list of values, and an array of values.
 *
 * @author Administrator
 */
public class TestBox {

    private ValueString subsub;
    private ValueInterface one;
    private ValueInterface[] arr;
    private List<ValueInterface> list;

    /**
     * Returns the subsub value.
     *
     * @return The ValueString instance.
     */
    public ValueString getSubsub() {
        return subsub;
    }

    /**
     * Sets the subsub value.
     *
     * @param subsub The ValueString instance to set.
     */
    public void setSubsub(ValueString subsub) {
        this.subsub = subsub;
    }

    /**
     * Returns the single value.
     *
     * @return The ValueInterface instance.
     */
    public ValueInterface getOne() {
        return one;
    }

    /**
     * Sets the single value.
     *
     * @param one The ValueInterface instance to set.
     */
    public void setOne(ValueInterface one) {
        this.one = one;
    }

    /**
     * Returns the array of values.
     *
     * @return The array of ValueInterface instances.
     */
    public ValueInterface[] getArr() {
        return arr;
    }

    /**
     * Sets the array of values.
     *
     * @param arr The array of ValueInterface instances to set.
     */
    public void setArr(ValueInterface[] arr) {
        this.arr = arr;
    }

    /**
     * Returns the list of values.
     *
     * @return The list of ValueInterface instances.
     */
    public List<ValueInterface> getList() {
        return list;
    }

    /**
     * Sets the list of values.
     *
     * @param list The list of ValueInterface instances to set.
     */
    public void setList(List<ValueInterface> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.valueOf(one));

        if (arr == null) {
            sb.append(", null");
        } else {
            sb.append(", [");
            boolean following = false;
            for (ValueInterface elem : arr) {
                if (following) {
                    sb.append(", ");
                }
                sb.append(String.valueOf(elem));
            }
            sb.append(']');
        }

        if (list == null) {
            sb.append(", null");
        } else {
            sb.append(", [");
            boolean following = false;
            for (ValueInterface elem : list) {
                if (following) {
                    sb.append(", ");
                }
                sb.append(String.valueOf(elem));
            }
            sb.append(']');
        }

        return sb.toString();
    }
}
