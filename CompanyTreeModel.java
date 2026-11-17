import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Builds the company tree hierarchy, grouping company names beneath their
 * sector nodes and keeping it synchronized with the earnings table model.
 */
public class CompanyTreeModel extends DefaultTreeModel {
    private final Map<CompanyEarnings, DefaultMutableTreeNode> companyNodes = new IdentityHashMap<>();

    /**
     * Creates a tree rooted at given String rootNodeName and populates it from the table model's current records.
     *
     * @param earningsModel the source of company records to display
     * @param rootNodeName  the name of the root node; header of JTree
     */
    public CompanyTreeModel(EarningsTableModel earningsModel, String rootNodeName) {
        super(new DefaultMutableTreeNode(rootNodeName));
        populateCompanyTree(earningsModel);
    }

    /**
     * Updates the tree label for a company whose name has changed.
     *
     * @param company the company record associated with the tree node
     */
    public void updateCompanyName(CompanyEarnings company) {
        DefaultMutableTreeNode companyNode = companyNodes.get(company);
        if (companyNode != null) {
            companyNode.setUserObject(company.getCompanyName());
            nodeChanged(companyNode);
        }
    }
    
    /**
     * Rebuilds the tree from the table model, refreshing sector groupings and
     * company nodes after records are added, removed, or changed.
     *
     * @param earningsModel the current source of company records
     */
    public void refresh(EarningsTableModel earningsModel) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
        root.removeAllChildren();
        companyNodes.clear();
        populateCompanyTree(earningsModel);
        reload(root);
    }

    public CompanyEarnings getCompanyEarningsFromNode(DefaultMutableTreeNode node) {
        return (CompanyEarnings) node.getUserObject();
    }


    /**
     * Adds each company beneath a sector node, preserving sector insertion
     * order and recording each company's corresponding tree node.
     *
     * @param earningsModel the source of companies to add to the tree
     */
    private void populateCompanyTree(EarningsTableModel earningsModel) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
        Map<String, DefaultMutableTreeNode> sectorNodes = new LinkedHashMap<>();

        for (int row = 0; row < earningsModel.getRowCount(); row++) {
            CompanyEarnings company = earningsModel.getCompanyAt(row);
            DefaultMutableTreeNode sectorNode = sectorNodes.computeIfAbsent(
                    company.getSector(),
                    sector -> {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sector);
                        root.add(node);
                        return node;
                    });
            DefaultMutableTreeNode companyNode = new DefaultMutableTreeNode(company.getCompanyName());
            sectorNode.add(companyNode);
            companyNodes.put(company, companyNode);
        }
    } 
}
