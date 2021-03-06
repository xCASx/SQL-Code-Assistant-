/*
 * Copyright (c) 2009,2010 Serhiy Kulyk
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 * SQL CODE ASSISTANT PLUG-IN FOR INTELLIJ IDEA IS PROVIDED BY SERHIY KULYK
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SERHIY KULYK BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.deepsky.lang.plsql.psi.impl.types;

import com.deepsky.lang.parser.plsql.PLSqlTypesAdopted;
import com.deepsky.lang.plsql.psi.PlSqlElementVisitor;
import com.deepsky.lang.plsql.psi.impl.PlSqlElementBase;
import com.deepsky.lang.plsql.psi.types.RowtypeType;
import com.deepsky.lang.plsql.struct.Type;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class RowtypeTypeImpl extends PlSqlElementBase implements RowtypeType {

    public RowtypeTypeImpl(ASTNode astNode) {
        super(astNode);
    }

    public Type getType() {
        return new com.deepsky.lang.plsql.struct.types.RowtypeType(
                getNode().findChildByType(PLSqlTypesAdopted.TABLE_REF).getText()
        );
    }

    public boolean isTypeValid() {
        final ASTNode t = getNode().findChildByType(PLSqlTypesAdopted.TABLE_REF);
        return t != null && getResolveFacade()
                .getLLResolver()
                .resolveTableRef(t.getText()) != null;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof PlSqlElementVisitor) {
            ((PlSqlElementVisitor) visitor).visitRowtypeType(this);
        } else {
            super.accept(visitor);
        }
    }

/*
    @NotNull
    public ResolveContext777 resolveType() throws NameNotResolvedException {
        String tableName = getNode().findChildByType(PLSqlTypesAdopted.TABLE_NAME).getText();
        TableDescriptor tdesc = describeTable(tableName);
        if (tdesc != null) {
            return new TableContext(getProject(), tdesc);
        } else {
            throw new NameNotResolvedException("Table " + tableName + " does not exist");
        }
    }
*/
}
