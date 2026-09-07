package relocate

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import kotlin.metadata.KmAnnotation
import kotlin.metadata.KmAnnotationArgument
import kotlin.metadata.KmClass
import kotlin.metadata.KmClassifier
import kotlin.metadata.KmConstructor
import kotlin.metadata.KmFunction
import kotlin.metadata.KmPackage
import kotlin.metadata.KmProperty
import kotlin.metadata.KmType
import kotlin.metadata.KmTypeAlias
import kotlin.metadata.KmTypeParameter
import kotlin.metadata.KmValueParameter
import kotlin.metadata.jvm.JvmFieldSignature
import kotlin.metadata.jvm.JvmMethodSignature
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.KotlinModuleMetadata
import kotlin.metadata.jvm.annotations
import kotlin.metadata.jvm.anonymousObjectOriginName
import kotlin.metadata.jvm.fieldSignature
import kotlin.metadata.jvm.getterSignature
import kotlin.metadata.jvm.lambdaClassOriginName
import kotlin.metadata.jvm.localDelegatedProperties
import kotlin.metadata.jvm.setterSignature
import kotlin.metadata.jvm.signature
import kotlin.metadata.jvm.syntheticMethodForAnnotations
import kotlin.metadata.jvm.syntheticMethodForDelegate

/**
 * Rewrites class names inside `@kotlin.Metadata` annotations and the `.kotlin_module` files under META-INF, so the Kotlin
 * compiler still sees relocated Kotlin classes (extension functions, companion objects, default arguments, ...)
 * as Kotlin declarations under their new names.
 *
 * Kotlin metadata uses `/` between packages and `.` before nested classes (`com/google/firebase/FirebaseOptions.Builder`),
 * so the slashed prefix mapping of [PackageRelocation] applies directly.
 */
class KotlinMetadataRemapper(private val relocation: PackageRelocation) {

    var remapped = 0
        private set
    var unreadable = 0
        private set

    private fun name(n: String): String = relocation.mapInternalName(n)
    private fun descriptor(d: String): String = relocation.mapString(d)

    /** Returns the remapped annotation, or null if the metadata cannot be read (the caller then strips it). */
    fun remap(metadata: Metadata): Metadata? {
        val km = try {
            KotlinClassMetadata.readStrict(metadata)
        } catch (e: Exception) {
            unreadable++
            return null
        }
        when (km) {
            is KotlinClassMetadata.Class -> remapClass(km.kmClass)
            is KotlinClassMetadata.FileFacade -> remapPackage(km.kmPackage)
            is KotlinClassMetadata.SyntheticClass -> km.kmLambda?.let { remapFunction(it.function) }
            is KotlinClassMetadata.MultiFileClassFacade -> km.partClassNames = km.partClassNames.map(::name)
            is KotlinClassMetadata.MultiFileClassPart -> {
                remapPackage(km.kmPackage)
                km.facadeClassName = name(km.facadeClassName)
            }
            is KotlinClassMetadata.Unknown -> {
                unreadable++
                return null
            }
        }
        remapped++
        return km.write()
    }

    fun remapModule(bytes: ByteArray): ByteArray {
        val module = KotlinModuleMetadata.read(bytes)
        val parts = module.kmModule.packageParts.toMap()
        module.kmModule.packageParts.clear()
        for ((packageName, packageParts) in parts) {
            val facades = packageParts.fileFacades.map(::name)
            val multiFile = packageParts.multiFileClassParts.entries.associate { (part, facade) -> name(part) to name(facade) }
            packageParts.fileFacades.clear()
            packageParts.fileFacades.addAll(facades)
            packageParts.multiFileClassParts.clear()
            packageParts.multiFileClassParts.putAll(multiFile)
            module.kmModule.packageParts[relocation.mapDotted(packageName)] = packageParts
        }
        return module.write()
    }

    private fun remapClass(c: KmClass) {
        c.name = name(c.name)
        c.typeParameters.forEach(::remapTypeParameter)
        c.supertypes.forEach(::remapType)
        c.functions.forEach(::remapFunction)
        c.properties.forEach(::remapProperty)
        c.constructors.forEach(::remapConstructor)
        c.typeAliases.forEach(::remapTypeAlias)
        c.sealedSubclasses.replaceAll(::name)
        c.inlineClassUnderlyingType?.let(::remapType)
        c.localDelegatedProperties.forEach(::remapProperty)
        c.anonymousObjectOriginName = c.anonymousObjectOriginName?.let(::name)
    }

    private fun remapPackage(p: KmPackage) {
        p.functions.forEach(::remapFunction)
        p.properties.forEach(::remapProperty)
        p.typeAliases.forEach(::remapTypeAlias)
        p.localDelegatedProperties.forEach(::remapProperty)
    }

    private fun remapFunction(f: KmFunction) {
        f.typeParameters.forEach(::remapTypeParameter)
        f.receiverParameterType?.let(::remapType)
        f.valueParameters.forEach(::remapValueParameter)
        remapType(f.returnType)
        f.signature = f.signature?.let { JvmMethodSignature(it.name, descriptor(it.descriptor)) }
        f.lambdaClassOriginName = f.lambdaClassOriginName?.let(::name)
    }

    private fun remapProperty(p: KmProperty) {
        p.typeParameters.forEach(::remapTypeParameter)
        p.receiverParameterType?.let(::remapType)
        p.setterParameter?.let(::remapValueParameter)
        remapType(p.returnType)
        p.fieldSignature = p.fieldSignature?.let { JvmFieldSignature(it.name, descriptor(it.descriptor)) }
        p.getterSignature = p.getterSignature?.let { JvmMethodSignature(it.name, descriptor(it.descriptor)) }
        p.setterSignature = p.setterSignature?.let { JvmMethodSignature(it.name, descriptor(it.descriptor)) }
        p.syntheticMethodForAnnotations = p.syntheticMethodForAnnotations?.let { JvmMethodSignature(it.name, descriptor(it.descriptor)) }
        p.syntheticMethodForDelegate = p.syntheticMethodForDelegate?.let { JvmMethodSignature(it.name, descriptor(it.descriptor)) }
    }

    private fun remapConstructor(c: KmConstructor) {
        c.valueParameters.forEach(::remapValueParameter)
        c.signature = c.signature?.let { JvmMethodSignature(it.name, descriptor(it.descriptor)) }
    }

    private fun remapTypeAlias(t: KmTypeAlias) {
        t.typeParameters.forEach(::remapTypeParameter)
        remapType(t.underlyingType)
        remapType(t.expandedType)
        t.annotations.replaceAll(::remapAnnotation)
    }

    private fun remapTypeParameter(t: KmTypeParameter) {
        t.upperBounds.forEach(::remapType)
        t.annotations.replaceAll(::remapAnnotation)
    }

    private fun remapValueParameter(v: KmValueParameter) {
        v.type.let(::remapType)
        v.varargElementType?.let(::remapType)
    }

    private fun remapType(t: KmType) {
        t.classifier = when (val classifier = t.classifier) {
            is KmClassifier.Class -> KmClassifier.Class(name(classifier.name))
            is KmClassifier.TypeAlias -> KmClassifier.TypeAlias(name(classifier.name))
            is KmClassifier.TypeParameter -> classifier
        }
        t.arguments.forEach { it.type?.let(::remapType) }
        t.abbreviatedType?.let(::remapType)
        t.outerType?.let(::remapType)
        t.flexibleTypeUpperBound?.type?.let(::remapType)
        t.annotations.replaceAll(::remapAnnotation)
    }

    private fun remapAnnotation(a: KmAnnotation): KmAnnotation =
        KmAnnotation(name(a.className), a.arguments.mapValues { (_, v) -> remapArgument(v) })

    private fun remapArgument(a: KmAnnotationArgument): KmAnnotationArgument = when (a) {
        is KmAnnotationArgument.KClassValue -> KmAnnotationArgument.KClassValue(name(a.className))
        is KmAnnotationArgument.ArrayKClassValue -> KmAnnotationArgument.ArrayKClassValue(name(a.className), a.arrayDimensionCount)
        is KmAnnotationArgument.EnumValue -> KmAnnotationArgument.EnumValue(name(a.enumClassName), a.enumEntryName)
        is KmAnnotationArgument.AnnotationValue -> KmAnnotationArgument.AnnotationValue(remapAnnotation(a.annotation))
        is KmAnnotationArgument.ArrayValue -> KmAnnotationArgument.ArrayValue(a.elements.map(::remapArgument))
        else -> a
    }
}

/**
 * ASM visitor that rewrites the `@kotlin.Metadata` annotation of a class. The rewritten annotation is written straight to
 * [target] (the class writer) so that no other remapper touches the protobuf payload in `d1`.
 */
class RemapKotlinMetadata(
    next: ClassVisitor,
    private val target: ClassVisitor,
    private val remapper: KotlinMetadataRemapper,
) : ClassVisitor(Opcodes.ASM9, next) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        if (descriptor != "Lkotlin/Metadata;") return super.visitAnnotation(descriptor, visible)
        return object : AnnotationVisitor(Opcodes.ASM9) {
            var kind = 1
            var metadataVersion = intArrayOf()
            var data1 = arrayOf<String>()
            var data2 = arrayOf<String>()
            var extraString = ""
            var packageName = ""
            var extraInt = 0

            override fun visit(name: String?, value: Any?) {
                when (name) {
                    "k" -> kind = value as Int
                    "mv" -> metadataVersion = value as IntArray
                    "xs" -> extraString = value as String
                    "pn" -> packageName = value as String
                    "xi" -> extraInt = value as Int
                }
            }

            override fun visitArray(name: String?): AnnotationVisitor {
                val values = mutableListOf<String>()
                return object : AnnotationVisitor(Opcodes.ASM9) {
                    override fun visit(name: String?, value: Any?) {
                        values += value as String
                    }

                    override fun visitEnd() {
                        when (name) {
                            "d1" -> data1 = values.toTypedArray()
                            "d2" -> data2 = values.toTypedArray()
                        }
                    }
                }
            }

            override fun visitEnd() {
                val original = Metadata(
                    kind = kind,
                    metadataVersion = metadataVersion,
                    data1 = data1,
                    data2 = data2,
                    extraString = extraString,
                    packageName = packageName,
                    extraInt = extraInt,
                )
                val mapped = remapper.remap(original) ?: return // unreadable: strip
                val out = target.visitAnnotation(descriptor, visible) ?: return
                out.visit("k", mapped.kind)
                out.visit("mv", mapped.metadataVersion)
                out.visitArray("d1").also { array -> mapped.data1.forEach { array.visit(null, it) }; array.visitEnd() }
                out.visitArray("d2").also { array -> mapped.data2.forEach { array.visit(null, it) }; array.visitEnd() }
                if (mapped.extraString.isNotEmpty()) out.visit("xs", mapped.extraString)
                if (mapped.packageName.isNotEmpty()) out.visit("pn", mapped.packageName)
                if (mapped.extraInt != 0) out.visit("xi", mapped.extraInt)
                out.visitEnd()
            }
        }
    }
}
