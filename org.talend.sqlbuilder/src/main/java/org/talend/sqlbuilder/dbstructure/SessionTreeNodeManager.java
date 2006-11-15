// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.sqlbuilder.dbstructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNode.EProperties;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.MetadataTableRepositoryObject;
import org.talend.sqlbuilder.dbstructure.nodes.CatalogNode;
import org.talend.sqlbuilder.dbstructure.nodes.DatabaseNode;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * 
 * This is a SessionTreeNode manager,It is used to process the creation, convertion,store of the SessionTreeNode. It can
 * convert DatabaseConnect to SessionTreeNode. SessionTreeNode is used in sql editor,sql result and database detail
 * viewer.
 * 
 * $Id: talend-code-templates.xml,v 1.3 2006/11/01 05:38:28 bqian Exp $
 * 
 */
public class SessionTreeNodeManager {

    // Used for SessionTreeNode cache
    private static Map<DatabaseConnection, SessionTreeNode> map = Collections
            .synchronizedMap(new HashMap<DatabaseConnection, SessionTreeNode>());

    /**
     * DOC qianbing Comment method "clear". Clears the cache.
     */
    public void clear() {
        map = Collections.synchronizedMap(new HashMap<DatabaseConnection, SessionTreeNode>());
    }

    /**
     * Converts the DatabaseConnection to SessionTreeNode, and stores the SessionTreeNode.
     * 
     * @param repositoryNode RepositoryNode
     * @return SessionTreeNode
     */
    public SessionTreeNode getSessionTreeNode(RepositoryNode repositoryNode) {
        // Gets the root RepositoryNode
        RepositoryNode root = getRoot(repositoryNode);
        // Gets the DatabaseConnection
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) root.getObject().getProperty().getItem())
                .getConnection();

        SessionTreeNode sessionTreeNode = map.get(connection);
        if (sessionTreeNode != null) {
            return sessionTreeNode;
        }
        // If the node is not existent,creates one and cache it.
        sessionTreeNode = SessionTreeNodeUtils.getSessionTreeNode("Repository name", connection.getDatabaseType(),
                connection.getURL(), connection.getUsername(), connection.getPassword(),
                connection.getSID().length() == 0 ? connection.getDatasourceName() : connection.getSID(), root);
        map.put(connection, sessionTreeNode);
        return sessionTreeNode;
    }

    /**
     * DOC qianbing Comment method "convert2INode". Converts the RepositoryNode input to INode. INode is used for the
     * sql editor,result viewer and the detail viewer.
     * 
     * @param repositoryNode RepositoryNode
     * @return INode
     */
    public INode convert2INode(RepositoryNode repositoryNode) {
        // Creates the SessionTreeNode.
        SessionTreeNode sessionTreeNode = getSessionTreeNode(repositoryNode);
        RepositoryNodeType nodeType = getRepositoryType(repositoryNode);
        if (nodeType.equals(RepositoryNodeType.DATABASE)) {
            // processes the database
            DatabaseModel model = sessionTreeNode.getDbModel();
            INode[] nodes = model.getChildNodes();
            DatabaseNode dn = (DatabaseNode) nodes[0];
            return dn;
        } else if (nodeType.equals(RepositoryNodeType.TABLE)) {
            // processes the table
            MetadataTableRepositoryObject tableObject = (MetadataTableRepositoryObject) repositoryNode.getObject();
            MetadataTable table = tableObject.getTable();
            String realName = table.getSourceName();
            System.out.println("real name : " + realName);

            DatabaseModel model = sessionTreeNode.getDbModel();
            INode[] nodes = model.getChildNodes();
            DatabaseNode dn = (DatabaseNode) nodes[0];
            nodes = dn.getChildNodes();
            CatalogNode cn = (CatalogNode) nodes[0];
            nodes = cn.getChildNodes();
            for (INode node : nodes) {
                TableNode tableNode = (TableNode) node;
                if (tableNode.getName().equals(realName)) {
                    return node;
                }
            }
        } else if (nodeType.equals(RepositoryNodeType.COLUMN)) {
            // Processes the column.
            // Gets the repositoryNode's parent,should be the repositoryNode of table infomation.
            repositoryNode = repositoryNode.getParent();
            return convert2INode(repositoryNode);
        }
        return null;
    }

    /**
     * DOC qianbing Comment method "getRepositoryType". Gets the type of the RepositoryNode.
     * 
     * @param repositoryNode RepositoryNode
     * @return RepositoryNodeType
     * @see RepositoryNodeType
     */
    private RepositoryNodeType getRepositoryType(RepositoryNode repositoryNode) {
        return (RepositoryNodeType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
    }

    /**
     * DOC qianbing Comment method "getRoot". Gets the root RepositoryNode of the input RepositoryNode. The root should
     * be a RepositoryNode with database infomation.
     * 
     * @param repositoryNode RepositoryNode
     * @return RepositoryNode
     */
    private RepositoryNode getRoot(RepositoryNode repositoryNode) {
        if (getRepositoryType(repositoryNode) == RepositoryNodeType.FOLDER) {
            throw new RuntimeException("RepositoryNode with folder info should not call this.");
        }

        if (getRepositoryType(repositoryNode) == RepositoryNodeType.DATABASE) {
            return repositoryNode;
        }
        return getRoot(repositoryNode.getParent());
    }
}
