/* RSAKeyPairX509Codec.java -- X.509 Encoding/Decoding handler
   Copyright (C) 2006 Free Software Foundation, Inc.

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


package gnu.java.security.key.rsa;

import gnu.java.security.OID;
import gnu.java.security.Registry;
import gnu.java.security.der.BitString;
import gnu.java.security.der.DER;
import gnu.java.security.der.DERReader;
import gnu.java.security.der.DERValue;
import gnu.java.security.der.DERWriter;
import gnu.java.security.key.IKeyPairCodec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 * An implementation of an {@link IKeyPairCodec} that knows how to encode /
 * decode X.509 ASN.1 external representation of RSA public keys.
 */
public class RSAKeyPairX509Codec
    implements IKeyPairCodec
{
  private static final OID RSA_ALG_OID = new OID(Registry.RSA_OID_STRING);

  // implicit 0-arguments constructor

  private static void checkIsConstructed(DERValue v, String msg)
  {
    if (! v.isConstructed())
      throw new InvalidParameterException(msg);
  }

  private static void checkIsBigInteger(DERValue v, String msg)
  {
    if (! (v.getValue() instanceof BigInteger))
      throw new InvalidParameterException(msg);
  }

  public int getFormatID()
  {
    return X509_FORMAT;
  }

  /**
   * Returns the X.509 ASN.1 <i>SubjectPublicKeyInfo</i> representation of an
   * RSA public key. The ASN.1 specification, as defined in RFC-3280, and
   * RFC-2459, is as follows:
   *
   * <pre>
   *   SubjectPublicKeyInfo ::= SEQUENCE {
   *     algorithm         AlgorithmIdentifier,
   *     subjectPublicKey  BIT STRING
   *   }
   *
   *   AlgorithmIdentifier ::= SEQUENCE {
   *     algorithm   OBJECT IDENTIFIER,
   *     parameters  ANY DEFINED BY algorithm OPTIONAL
   *   }
   * </pre>
   * 
   * <p>The <i>subjectPublicKey</i> field, which is a BIT STRING, contains the
   * DER-encoded form of the RSA public key defined as:</p>
   * 
   * <pre>
   *   RSAPublicKey ::= SEQUENCE {
   *     modulus         INTEGER, -- n
   *     publicExponent  INTEGER  -- e
   *   }
   * </pre>
   * 
   * @param key the {@link PublicKey} instance to encode. MUST be an instance of
   *          {@link GnuRSAPublicKey}.
   * @return the ASN.1 representation of the <i>SubjectPublicKeyInfo</i> in an
   *         X.509 certificate.
   * @throw InvalidParameterException if <code>key</code> is not an instance
   *        of {@link GnuRSAPublicKey} or if an exception occurs during the
   *        marshalling process.
   */
  public byte[] encodePublicKey(PublicKey key)
  {
    if (! (key instanceof GnuRSAPublicKey))
      throw new InvalidParameterException("key");

    DERValue derOID = new DERValue(DER.OBJECT_IDENTIFIER, RSA_ALG_OID);

    GnuRSAPublicKey rsaKey = (GnuRSAPublicKey) key;
    BigInteger n = rsaKey.getN();
    BigInteger e = rsaKey.getE();

    DERValue derN = new DERValue(DER.INTEGER, n);
    DERValue derE = new DERValue(DER.INTEGER, e);

    ArrayList algorithmID = new ArrayList(1);
    algorithmID.add(derOID);
    DERValue derAlgorithmID = new DERValue(DER.CONSTRUCTED | DER.SEQUENCE,
                                           algorithmID);

    ArrayList publicKey = new ArrayList(2);
    publicKey.add(derN);
    publicKey.add(derE);
    DERValue derPublicKey = new DERValue(DER.CONSTRUCTED | DER.SEQUENCE,
                                         publicKey);
    byte[] spkBytes = derPublicKey.getEncoded();
    DERValue derSPK = new DERValue(DER.BIT_STRING, new BitString(spkBytes));

    ArrayList spki = new ArrayList(2);
    spki.add(derAlgorithmID);
    spki.add(derSPK);
    DERValue derSPKI = new DERValue(DER.CONSTRUCTED | DER.SEQUENCE, spki);

    byte[] result;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try
      {
        DERWriter.write(baos, derSPKI);
        result = baos.toByteArray();
      }
    catch (IOException x)
      {
        InvalidParameterException y = new InvalidParameterException();
        y.initCause(x);
        throw y;
      }

    return result;
  }

  public byte[] encodePrivateKey(PrivateKey key)
  {
    throw new IllegalArgumentException("Wrong format for private keys");
  }

  /**
   * @param input the byte array to unmarshall into a valid RSA
   *          {@link PublicKey} instance. MUST NOT be null.
   * @return a new instance of a {@link GnuRSAPublicKey} decoded from the
   *         <i>SubjectPublicKeyInfo</i> material in an X.509 certificate.
   * @throw InvalidParameterException if an exception occurs during the
   *        unmarshalling process.
   */
  public PublicKey decodePublicKey(byte[] input)
  {
    if (input == null)
      throw new InvalidParameterException("Input bytes MUST NOT be null");

    BigInteger n, e;
    DERReader der = new DERReader(input);
    try
      {
        DERValue derSPKI = der.read();
        checkIsConstructed(derSPKI, "Wrong SubjectPublicKeyInfo field");

        DERValue derAlgorithmID = der.read();
        checkIsConstructed(derAlgorithmID, "Wrong AlgorithmIdentifier field");

        DERValue derOID = der.read();
        if (! (derOID.getValue() instanceof OID))
          throw new InvalidParameterException("Wrong Algorithm field");

        OID algOID = (OID) derOID.getValue();
        if (! algOID.equals(RSA_ALG_OID))
          throw new IllegalArgumentException("Unexpected OID: " + algOID);

        DERValue val = der.read();
        if (! (val.getValue() instanceof BitString))
          throw new InvalidParameterException("Wrong SubjectPublicKey field");

        byte[] spkBytes = ((BitString) val.getValue()).toByteArray();

        der = new DERReader(spkBytes);
        val = der.read();
        checkIsConstructed(derAlgorithmID, "Wrong subjectPublicKey field");

        val = der.read();
        checkIsBigInteger(val, "Wrong modulus field");
        n = (BigInteger) val.getValue();
        val = der.read();
        checkIsBigInteger(val, "Wrong publicExponent field");
        e = (BigInteger) val.getValue();
      }
    catch (IOException x)
      {
        InvalidParameterException y = new InvalidParameterException();
        y.initCause(x);
        throw y;
      }

    return new GnuRSAPublicKey(Registry.X509_ENCODING_ID, n, e);
  }

  public PrivateKey decodePrivateKey(byte[] input)
  {
    throw new IllegalArgumentException("Wrong format for private keys");
  }
}