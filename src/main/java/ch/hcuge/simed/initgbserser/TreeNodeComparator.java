package ch.hcuge.simed.initgbserser;

import java.util.Comparator;

public class TreeNodeComparator implements Comparator<TreeNode>{
	@Override
	public int compare(TreeNode o1, TreeNode o2) {
		// TODO Auto-generated method stub
		return o1.getName().compareTo(o2.getName());
	}
}
