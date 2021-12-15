// Written by Valentin HENRY
//
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package blaireau.dsl

import blaireau.metas.{FieldProduct, Meta, MetaElt, MetaField}
import shapeless.HList
import shapeless.ops.hlist.ToList
import skunk.Codec

class Table[T, F <: HList, MF <: HList](name: String, val meta: Meta.Aux[T, F, MF]) {
  type SelectQuery[S <: HList, SC] = SelectQueryBuilder[T, F, MF, S, SC, skunk.Void]
  private[this] def select[S <: HList, SC](selects: S, selectCodec: Codec[SC])(implicit
    toList: ToList[S, MetaField[_]]
  ): SelectQuery[S, SC] =
    new SelectQueryBuilder[T, F, MF, S, SC, skunk.Void](
      name,
      meta,
      selects,
      selectCodec,
      where = Action.BooleanOp.empty
    )

  def select(implicit
    toList: ToList[MF, MetaField[_]]
  ): SelectQuery[MF, T] = select(meta.metaFields, meta.codec)

  def select[MFO <: HList, OC](s: MetaElt.Aux[T, F, MF] => FieldProduct.Aux[OC, MFO])(implicit
    toList: ToList[MFO, MetaField[_]]
  ): SelectQuery[MFO, OC] = {
    val selected = s(meta)
    select(selected.metaFields, selected.codec)
  }
}

object Table {
  def apply[T](name: String)(implicit m: Meta[T]): Table[T, m.F, m.MF] = new Table[T, m.F, m.MF](name, m)
}
