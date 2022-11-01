package MySymbolTable;

import Lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, SymbolTableItem> items;
    private ArrayList<SymbolTable> childTable;
    private SymbolTable parentTable;
    private ArrayList<SymbolTableItem> orderedItems;

    public SymbolTable() {
        items = new HashMap<String, SymbolTableItem>();
        childTable = new ArrayList<SymbolTable>();
        orderedItems = new ArrayList<>();
        parentTable = null;
    }

    public void addItem(SymbolTableItem item) {
        items.put(item.getName(), item);
        orderedItems.add(item);
    }

    public SymbolTable generateChildTable() {
        SymbolTable child = new SymbolTable();
        child.setParentTable(this);
        childTable.add(child);
        return child;
    }

    public SymbolTable getParentTable() {
        return parentTable;
    }

    public void setParentTable(SymbolTable parent) {
        this.parentTable = parent;
    }

    public HashMap<String, SymbolTableItem> getItems() {
        return items;
    }

    public ArrayList<SymbolTableItem> getOrderedItems() {
        return orderedItems;
    }

    public SymbolTableItem findItemInThisTable(Token ident) {
        return items.getOrDefault(ident.getValue(), null);
    }

    public static SymbolTableItem findItemFromAllTable(Token ident, SymbolTable symbolTable) {
        SymbolTable mySymbolTable = symbolTable;
        while (mySymbolTable != null) {
            SymbolTableItem temp = mySymbolTable.findItemInThisTable(ident);
            if (temp != null && temp.getLine() <= ident.getLine()) {
                return temp;
            }
            mySymbolTable = mySymbolTable.getParentTable();
        }
        return null;
    }
}
