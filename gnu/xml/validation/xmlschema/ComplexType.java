/* ComplexType.java --
   Copyright (C) 2006  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package gnu.xml.validation.xmlschema;

import gnu.xml.validation.datatype.Annotation;
import gnu.xml.validation.datatype.Type;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * A complex type definition.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class ComplexType
  extends Type
{

  /**
   * Either a simple type definition or a complex type definition.
   */
  QName baseType;

  /**
   * Either EXTENSION or RESTRICTION.
   */
  int derivationMethod;

  /**
   * A subset of {EXTENSION, RESTRICTION}.
   */
  final int finality;

  final boolean isAbstract;

  // Currently both AttributeUse and QName objects
  // are added to this.
  Set<Object> attributeUses;

  AnyAttribute attributeWildcard;

  /**
   * One of EMPTY, SIMPLE, MIXED, or ELEMENT_ONLY.
   */
  int contentType;

  /**
   * A simple type definition or a Particle.
   */
  Object contentModel;

  final int prohibitedSubstitutions;

  Set<Annotation> annotations;

  ComplexType(QName name,
              boolean isAbstract,
              int prohibitedSubstitutions,
              int finality)
  {
    super(name);
    this.isAbstract = isAbstract;
    this.prohibitedSubstitutions = prohibitedSubstitutions;
    this.finality = finality;
    attributeUses = new LinkedHashSet<Object>();
  }

}
