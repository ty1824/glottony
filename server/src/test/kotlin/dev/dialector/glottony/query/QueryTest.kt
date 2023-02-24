package dev.dialector.glottony.query

import dev.dialector.glottony.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class QueryTest {
    @Test
    fun testBasicParsing() {
        val db = Database()

        val fooFile = FileId("foo.glot")
        val firstFileText = """
            |fun foo(): string = { 
            |    val v : string = "hello" 
            |    return 5
            |}
            """.trimMargin()
        db.setFileText(fooFile, firstFileText)

        assertEquals(firstFileText, db.fileText(fooFile))

        db.parseFile(fooFile)!!.rootNode.let { ast ->
            assertEquals(1, ast.contents.size)
            assert(ast.contents[0] is FunctionDeclaration)
            val decl = ast.contents[0] as FunctionDeclaration
            assertEquals("foo", decl.name )
            assertTrue(decl.type is StringType, "Expected string but was ${decl.type}")
            assertTrue(decl.body is BlockExpression, "Expected block expression but was ${decl.body}")
            val blockExpression = decl.body as BlockExpression
            val firstStatement = blockExpression.block.statements[0]
            assertTrue(firstStatement is ValStatement, "Expected val statement but was $firstStatement")

            // Verify that running parse again uses memoized value
            assertSame(ast, db.parseFile(fooFile)!!.rootNode)

            val diagnostics = db.diagnostics(fooFile)!!
            println(diagnostics)
            assertEquals(1, diagnostics.size)

            assertSame(diagnostics, db.diagnostics(fooFile))
        }

        val secondFileText = """
            |fun bar(): string = { 
            |    val v : string = "hello" 
            |    return "hi"
            |}
            """.trimMargin()
        db.setFileText(fooFile, secondFileText)

        db.parseFile(fooFile)!!.rootNode.let { ast ->
            assertEquals(1, ast.contents.size)
            assert(ast.contents[0] is FunctionDeclaration)
            val decl = ast.contents[0] as FunctionDeclaration
            assertEquals("bar", decl.name)
            assertTrue(decl.type is StringType, "Expected string but was ${decl.type}")
            assertTrue(decl.body is BlockExpression, "Expected block expression but was ${decl.body}")
            val blockExpression = decl.body as BlockExpression
            val firstStatement = blockExpression.block.statements[0]
            assertTrue(firstStatement is ValStatement, "Expected val statement but was $firstStatement")
            val lastStatement = blockExpression.block.statements[1]
            assertTrue(lastStatement is ReturnStatement)
            assertEquals("hi", (lastStatement.expression as StringLiteral).value)

            val diagnostics = db.diagnostics(fooFile)!!
            assertEquals(0, diagnostics.size)

            assertSame(diagnostics, db.diagnostics(fooFile))
        }
    }
}