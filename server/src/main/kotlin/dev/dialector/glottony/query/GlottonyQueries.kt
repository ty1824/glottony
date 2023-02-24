package dev.dialector.glottony.query

import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.ast.File
import dev.dialector.glottony.diagnostics.GlottonyDiagnosticProvider
import dev.dialector.glottony.diagnostics.ModelDiagnostic
import dev.dialector.glottony.parser.GlottonyParser
import dev.dialector.glottony.parser.ParseResult
import dev.dialector.glottony.typesystem.GlottonyTypesystem
import dev.dialector.query.*

class GlottonyQueries {
}

class FileId(val id: String)

interface SourceQueries {
    @Input
    fun fileText(id: FileId): String?
}

interface SyntaxQueries {
    @Tracked
    fun parseFile(id: FileId): GlottonyRoot?
}

interface DiagnosticQueries {
    @Tracked
    fun diagnostics(id: FileId): List<ModelDiagnostic>?
}

class Database : SourceQueries, SyntaxQueries, DiagnosticQueries {
    private object SourceQueriesDef : QueryGroupDef<SourceQueries>(SourceQueries::class)
    private object SyntaxQueriesDef : QueryGroupDef<SyntaxQueries>(SyntaxQueries::class)
    private object DiagnosticQueriesDef : QueryGroupDef<DiagnosticQueries>(DiagnosticQueries::class)
    private object FileText : InputQuery<FileId, String?>(SourceQueriesDef)
    private object ParseFile : DerivedQuery<FileId, GlottonyRoot?>(SyntaxQueriesDef)
    private object Diagnostics : DerivedQuery<FileId, List<ModelDiagnostic>?>(DiagnosticQueriesDef)

    private val database = QueryDatabase(listOf(FileText, ParseFile, Diagnostics))

    fun setFileText(id: FileId, text: String?) = database.setInput(FileText, id, text)

    override fun fileText(id: FileId): String? = database.inputQuery(FileText, id)

    override fun parseFile(id: FileId): GlottonyRoot? = database.derivedQuery(ParseFile, id) {
        println(">> Parsing: ${id.id}")
        val (result, map) = GlottonyParser.parseStringWithSourceMap(fileText(it)!!)
        GlottonyRoot(result, map)
    }

    override fun diagnostics(id: FileId): List<ModelDiagnostic>? = database.derivedQuery(Diagnostics, id) {
        println(">> Evaluating diagnostics: ${id.id}")
        val diagnosticProvider = GlottonyDiagnosticProvider(GlottonyTypesystem())
        diagnosticProvider.run(parseFile(id)!!)
    }
}