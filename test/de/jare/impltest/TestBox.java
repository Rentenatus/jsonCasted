package de.jare.impltest;

import java.util.List;

/**
 *
 * @author Administrator
 */
public class TestBox {

    private ValueString subsub;
    private ValueInterface one;
    private ValueInterface[] arr;
    private List<ValueInterface> list;

    public ValueString getSubsub() {
        return subsub;
    }

    public void setSubsub(ValueString subsub) {
        this.subsub = subsub;
    }

    public ValueInterface getOne() {
        return one;
    }

    public void setOne(ValueInterface one) {
        this.one = one;
    }

    public ValueInterface[] getArr() {
        return arr;
    }

    public void setArr(ValueInterface[] arr) {
        this.arr = arr;
    }

    public List<ValueInterface> getList() {
        return list;
    }

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
