/*
 * Copyright (C) 2023 Hal Perkins.  All rights reserved.  Permission is
 * hereby granted to students registered for University of Washington
 * CSE 331 for use solely during Winter Quarter 2023 for purposes of
 * the course.  No other use, copying, distribution, or modification
 * is permitted without prior written consent. Copyrights for
 * third-party components of this work must be honored.  Instructors
 * interested in reusing these course materials should contact the
 * author.
 */

package poly;

import java.util.*;

/**
 * <b>RatPoly</b> represents an immutable single-variate polynomial expression. RatPolys are sums of
 * RatTerms with non-negative exponents.
 *
 * <p>Examples of RatPolys include "0", "x-10", and "x^3-2*x^2+5/3*x+3", and "NaN".
 */
// See RatNum's documentation for a definition of "immutable".
public final class RatPoly {

    /**
     * Holds all the RatTerms in this RatPoly.
     */
    private final List<RatTerm> terms;

    // Definitions:
    // For a RatPoly p, let C(p,i) be "p.terms.get(i).getCoeff()" and
    // E(p,i) be "p.terms.get(i).getExpt()"
    // length(p) be "p.terms.size()"
    // (These are helper functions that will make it easier for us
    // to write the remainder of the specifications. They are not
    // executable code; they just represent complex expressions in a
    // concise manner, so that we can stress the important parts of
    // other expressions in the spec rather than get bogged down in
    // the details of how we extract the coefficient for the 2nd term
    // or the exponent for the 5th term. So when you see C(p,i),
    // think "coefficient for the ith term in p".)
    //
    // Abstraction Function:
    // RatPoly, p, represents the polynomial equal to the sum of the
    // RatTerms contained in 'terms':
    // sum (0 <= i < length(p)): p.terms.get(i)
    // If there are no terms, then the RatPoly represents the zero
    // polynomial.
    //
    // Representation Invariant for every RatPoly p:
    // terms != null &&
    // forall i such that (0 <= i < length(p)), C(p,i) != 0 &&
    // forall i such that (0 <= i < length(p)), E(p,i) >= 0 &&
    // forall i such that (0 <= i < length(p) - 1), E(p,i) > E(p, i+1)
    // In other words:
    // * The terms field always points to some usable object.
    // * No term in a RatPoly has a zero coefficient.
    // * No term in a RatPoly has a negative exponent.
    // * The terms in a RatPoly are sorted in descending exponent order.
    // (It is implied that 'terms' does not contain any null elements by the
    // above invariant.)

    /**
     * A constant holding a Not-a-Number (NaN) value of type RatPoly.
     */
    public static final RatPoly NaN = new RatPoly(RatTerm.NaN);

    /**
     * A constant holding a zero value of type RatPoly.
     */
    public static final RatPoly ZERO = new RatPoly();

    /**
     * Constructs a new RatPoly.
     *
     * @spec.effects Constructs a new Poly, "0".
     */
    public RatPoly() {
        this(RatTerm.ZERO);
        checkRep();
    }

    /**
     * Constructs a new RatPoly.
     *
     * @param rt the single term which the new RatPoly equals
     * @spec.requires {@code rt.getExpt() >= 0}
     * @spec.effects Constructs a new Poly equal to "rt". If rt.isZero(), constructs a "0"
     * polynomial.
     */
    public RatPoly(RatTerm rt) {
        this.terms = new ArrayList<>();
        if(!rt.isZero() && rt.getExpt() >= 0) {
            this.terms.add(rt);
        }
        checkRep();
    }

    /**
     * Constructs a new RatPoly.
     *
     * @param c the constant in the term which the new RatPoly equals
     * @param e the exponent in the term which the new RatPoly equals
     * @spec.requires {@code e >= 0}
     * @spec.effects Constructs a new Poly equal to "c*x^e". If c is zero, constructs a "0"
     * polynomial.
     */
    public RatPoly(int c, int e) {
        this(new RatTerm(new RatNum(c), e));
        checkRep();
    }

    /**
     * Constructs a new RatPoly.
     *
     * @param rt a list of terms to be contained in the new RatPoly
     * @spec.requires 'rt' satisfies clauses given in rep. invariant
     * @spec.effects Constructs a new Poly using 'rt' as part of the representation. The method does
     * not make a copy of 'rt'.
     */
    private RatPoly(List<RatTerm> rt) {
        terms = rt;
        // The spec tells us that we don't need to make a copy of 'rt'
        checkRep();
    }

    /**
     * Throws an exception if the representation invariant is violated.
     */
    private void checkRep() {
        assert (this.terms != null);

        for(int i = 0; i < this.terms.size(); i++) {
            assert (!this.terms.get(i).getCoeff().equals(new RatNum(0))) : "zero coefficient";
            assert (this.terms.get(i).getExpt() >= 0) : "negative exponent";

            if(i < this.terms.size() - 1) {
                assert (this.terms.get(i + 1).getExpt() < this.terms.get(i).getExpt()) : "terms out of order";
            }
        }
    }

    /**
     * Returns the degree of this RatPoly.
     *
     * @return the largest exponent with a non-zero coefficient, or 0 if this is "0".
     * @spec.requires !this.isNaN()
     */
    public int degree() {
        checkRep();
        if(this.terms.size() != 0) {
            return this.terms.get(0).getExpt();
        } else {
            return 0;
        }
    }

    /**
     * Gets the RatTerm associated with degree 'deg'.
     *
     * @param deg the degree for which to find the corresponding RatTerm
     * @return the RatTerm of degree 'deg'. If there is no term of degree 'deg' in this poly, then
     * returns the zero RatTerm.
     * @spec.requires !this.isNaN()
     */
    public RatTerm getTerm(int deg) {
        checkRep();
        //{inv:[0...i - 1] has been explored for the term with the given degree,
        // where i is the index variable in the for-loop
        for(int i = 0; i < this.terms.size(); i++) {
            if(deg == this.terms.get(i).getExpt()) {
                return this.terms.get(i);
            }
        }
        checkRep();
        return RatTerm.ZERO;
    }

    /**
     * Returns true if this RatPoly is not-a-number.
     *
     * @return true if and only if this has some coefficient = "NaN".
     */
    public boolean isNaN() {
        checkRep();
        if(this.terms.size() != 0) {
            return (this.terms.get(0).isNaN());
        } else {
            return false;
        }
    }

    /**
     * Scales coefficients within a list of terms (helper procedure).
     *
     * @param lst    contains the RatTerms to be scaled
     * @param scalar the value by which to scale coefficients in lst
     * @spec.requires lst, scalar != null
     * @spec,modifies none
     * @returns: Returns a new RatPoly, called result, based on lst
     * where it's terms have been scaled. Where lst had (C . E),
     * result has (C * scalar . E) for all terms in lst.
     * @see RatTerm regarding (C . E) notation
     */
    private static RatPoly scaleCoeff(List<RatTerm> lst, RatNum scalar) {
        RatPoly result = new RatPoly();
        if(scalar.compareTo(RatNum.ZERO) == 0) {
            return result;
        }
        result.terms.addAll(lst);
        //{inv:[C(p,0). . .C(p,i - 1)] has been scaled. The set represents the
        // coefficients of the terms in lst that
        // have been scaled.
        for(int i = 0; i < result.terms.size(); i++) {
            RatNum coefficient = result.terms.get(i).getCoeff().mul(scalar);
            int exponent = result.terms.get(i).getExpt();
            result.terms.set(i, new RatTerm(coefficient,exponent));
        }
        return result;
    }

    /**
     * Returns a new RatPoly where a given list of terms, their exponents will be
     * incremented by a 'degree' (helper procedure).
     *
     * @param lst    contains the RatTerms whose exponents are to be incremented
     * @param degree the value by which to increment exponents in lst
     * @spec.requires lst != null
     * @spec.modifies none
     * @returns: Returns a new RatPoly, called result, based on lst
     * but with its exponents incremented. Where lst had (C . E),
     * result has (C . E + degree) for all terms.
     * @see RatTerm regarding (C . E) notation
     */
    private static RatPoly incremExpt(List<RatTerm> lst, int degree) {
        //{inv:[0. . .i - 1] has been incremented. The set represents the exponents of
        // the RatTerm in the lst that have been incremented.
        RatPoly result = new RatPoly();
        for(int i = 0; i < lst.size(); i++) {
            int exponent = lst.get(i).getExpt() + degree;
            if(exponent >= 0) {
                sortedInsert(result.terms, new RatTerm(lst.get(i).getCoeff(), exponent));
            }
        }
        return result;
    }

    /**
     * Inserts a term into a sorted sequence of terms, preserving the sorted nature of the sequence.
     * If a term with the given degree already exists, adds their coefficients (helper procedure).
     *
     * <p>Definitions: Let a "Sorted List<RatTerm>" be a List<RatTerm> V such that [1] V is sorted in
     * descending exponent order && [2] there are no two RatTerms with the same exponent in V && [3]
     * there is no RatTerm in V with a coefficient equal to zero
     *
     * <p>For a Sorted List<RatTerm> V and integer e, let cofind(V, e) be either the coefficient for a
     * RatTerm rt in V whose exponent is e, or zero if there does not exist any such RatTerm in V.
     * (This is like the coeff function of RatPoly.) We will write sorted(lst) to denote that lst is a
     * Sorted List<RatTerm>, as defined above.
     *
     * @param lst     the list into which newTerm should be inserted
     * @param newTerm the term to be inserted into the list
     * @spec.requires lst != null && sorted(lst)
     * @spec.modifies lst
     * @spec.effects sorted(lst_post) && (cofind(lst_post,newTerm.getExpt()) =
     * cofind(lst,newTerm.getExpt()) + newTerm.getCoeff()).
     */
    private static void sortedInsert(List<RatTerm> lst, RatTerm newTerm) {
        Comparator<RatTerm> compareRatTerms = new Comparator<>() {
            @Override
            //Compares two variables of type RatTerm and returns a
            // -1, 0, or 1 as the first variable is greater than,
            // equal to, or less than the second.

            //@param: RatTerm r1 and RatTerm r2 will be compared based
            // on their exponent.
            //@spec.requires: RatTerm r1 and RatTerm r2 must not be null.
            //@return: Returns -1 if r1 has a greater exponent, 1 if r2
            // has a greater exponent, and 0 if they have the same
            // exponent

            public int compare(RatTerm r1, RatTerm r2) {
                if (r1.getExpt() < r2.getExpt()) {
                    return 1;
                } else if (r1.getExpt() > r2.getExpt()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
        boolean isItThere = false;
        if(!newTerm.isZero()) {
            for(int i = 0; i < lst.size(); i++) {
                if(lst.get(i).getExpt() == newTerm.getExpt()) {
                    RatNum newCoeff = lst.get(i).getCoeff().add(newTerm.getCoeff());
                    if(newCoeff.compareTo(RatNum.ZERO) == 0) {
                        lst.remove(i);
                    } else {
                        lst.set(i, new RatTerm (newCoeff, lst.get(i).getExpt()));
                    }
                    isItThere = true;
                }
            }
            if(!isItThere) {
                lst.add(newTerm);
            }
        }
        Collections.sort(lst,compareRatTerms);
    }

    /**
     * Return the additive inverse of this RatPoly.
     *
     * @return a RatPoly equal to "0 - this"; if this.isNaN(), returns some r such that r.isNaN().
     */
    public RatPoly negate() {
        checkRep();
        if(this.isNaN()) {
            return NaN;
        } else {
            RatPoly result = scaleCoeff(this.terms,new RatNum(-1));
            checkRep();
            return result;
        }
    }

    /**
     * Addition operation.
     *
     * @param p the other value to be added
     * @return a RatPoly, r, such that r = "this + p"; if this.isNaN() or p.isNaN(), returns some r
     * such that r.isNaN().
     * @spec.requires p != null
     */
    public RatPoly add(RatPoly p) {
        checkRep();
        if (this.isNaN() || p.isNaN()) {
            return NaN;
        } else {
            RatPoly result = new RatPoly();
            result.terms.addAll(this.terms);
            //{inv: result = this.terms + p.terms && result = q.terms + p.terms.get(0) +... + p.terms.get(i-1)
            // where p.terms(i) is the ith term in p.terms}
            for(int i = 0; i < p.terms.size(); i++) {
                sortedInsert(result.terms,p.terms.get(i));
            }
            checkRep();
            return result;
        }
    }

    /**
     * Subtraction operation.
     *
     * @param p the value to be subtracted
     * @return a RatPoly, r, such that r = "this - p"; if this.isNaN() or p.isNaN(), returns some r
     * such that r.isNaN().
     * @spec.requires p != null
     */
    public RatPoly sub(RatPoly p) {
        checkRep();
        if (this.isNaN() || p.isNaN()) {
            return NaN;
        } else {
            RatPoly result = new RatPoly();
            result.terms.addAll(this.terms);
            //{inv: result = this.terms - p.terms && result = q.terms - p.terms.get(0) -... - p.terms.get(i-1)
            // where p.terms(i) is the ith term in p.terms}
            for(int i = 0; i < p.terms.size(); i++) {
                sortedInsert(result.terms,p.terms.get(i).negate());
            }
            checkRep();
            return result;
        }
    }


    /**
     * Multiplication operation.
     *
     * @param p the other value to be multiplied
     * @return a RatPoly, r, such that r = "this * p"; if this.isNaN() or p.isNaN(), returns some r
     * such that r.isNaN().
     * @spec.requires p != null
     */
    public RatPoly mul(RatPoly p) {
        checkRep();
        if (this.isNaN() || p.isNaN()) {
            return NaN;
        } else {
            RatPoly result = new RatPoly();
            if (p.terms.size() != 0) {
                RatPoly storage = scaleCoeff(this.terms,p.terms.get(0).getCoeff());
                storage = incremExpt(storage.terms,p.terms.get(0).getExpt());
                result = storage;
                //{result = this.terms * p.terms[0. . i-1] =
                // (this.terms * p.terms_0) + . . . + (this.terms * p.terms_i - 1)
                // where p.terms_i is the ith term in p. Every term in this RatPoly is multiplied
                // by p.terms_i, and this.term represents this.terms_0 . . this.terms_1 . . this.terms_size-1
                for(int i = 1; i < p.terms.size(); i++) {
                    storage = scaleCoeff(this.terms,p.terms.get(i).getCoeff());
                    storage = incremExpt(storage.terms,p.terms.get(i).getExpt());
                    result = result.add(storage);
                }
            }
            checkRep();
            return result;
        }
    }

    /**
     * Truncating division operation.
     *
     * <p>Truncating division gives the number of whole times that the divisor is contained within
     * the dividend. That is, truncating division disregards or discards the remainder. Over the
     * integers, truncating division is sometimes called integer division; for example, 10/3=3,
     * 15/2=7.
     *
     * <p>Here is a formal way to define truncating division: u/v = q, if there exists some r such
     * that:
     *
     * <ul>
     * <li>u = q * v + r<br>
     * <li>The degree of r is strictly less than the degree of v.
     * <li>The degree of q is no greater than the degree of u.
     * <li>r and q have no negative exponents.
     * </ul>
     * <p>
     * q is called the "quotient" and is the result of truncating division. r is called the
     * "remainder" and is discarded.
     *
     * <p>Here are examples of truncating division:
     *
     * <ul>
     * <li>"x^3-2*x+3" / "3*x^2" = "1/3*x" (with r = "-2*x+3")
     * <li>"x^2+2*x+15 / 2*x^3" = "0" (with r = "x^2+2*x+15")
     * <li>"x^3+x-1 / x+1 = x^2-x+2 (with r = "-3")
     * </ul>
     *
     * @param p the divisor
     * @return the result of truncating division, {@code this / p}. If p = 0 or this.isNaN() or
     * p.isNaN(), returns some q such that q.isNaN().
     * @spec.requires p != null
     */
    public RatPoly div(RatPoly p) {
        checkRep();
        if ((this.isNaN() || p.isNaN()) || p.terms.size() == 0) {
            return NaN;
        }
        RatPoly quotient = new RatPoly();
        RatPoly lst = new RatPoly();
        lst.terms.addAll(this.terms);
        //{inv: (this.terms)_pre = quotient * p + lst.terms where
        // (lst.terms) is the dividend/polynomial
        // we are dividing from.
        while(lst.terms.size() != 0 && lst.degree() >= p.degree()) {
            RatNum num = lst.terms.get(0).getCoeff();
            RatNum denom = p.terms.get(0).getCoeff();
            int exponent = lst.terms.get(0).getExpt() - p.terms.get(0).getExpt();
            RatTerm termInQuotient = new RatTerm(num.div(denom),exponent);
            RatPoly partOfQuotient = new RatPoly(termInQuotient);
            quotient = quotient.add(new RatPoly(termInQuotient));
            lst = lst.sub(p.mul(partOfQuotient));
        }
        checkRep();
        return quotient;
    }

    /**
     * Returns the value of this RatPoly, evaluated at d.
     *
     * @param d the value at which to evaluate this polynomial
     * @return the value of this polynomial when evaluated at 'd'. For example, "x+2" evaluated at 3
     * is 5, and "x^2-x" evaluated at 3 is 6. If (this.isNaN() == true), return Double.NaN.
     */
    public double eval(double d) {
        checkRep();
        if(this.isNaN()) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < this.terms.size(); i++) {
            sum += this.terms.get(i).eval(d);
        }
        checkRep();
        return sum;
    }

    /**
     * Returns a string representation of this RatPoly. Valid example outputs include
     * "x^17-3/2*x^2+1", "-x+1", "-1/2", and "0".
     *
     * @return a String representation of the expression represented by this, with the terms sorted
     * in order of degree from highest to lowest.
     * <p>There is no whitespace in the returned string.
     * <p>If the polynomial is itself zero, the returned string will just be "0".
     * <p>If this.isNaN(), then the returned string will be just "NaN".
     * <p>The string for a non-zero, non-NaN poly is in the form "(-)T(+|-)T(+|-)...", where "(-)"
     * refers to a possible minus sign, if needed, and "(+|-)" refers to either a plus or minus
     * sign. For each term, T takes the form "C*x^E" or "C*x" where {@code C > 0}, UNLESS: (1) the
     * exponent E is zero, in which case T takes the form "C", or (2) the coefficient C is one, in
     * which case T takes the form "x^E" or "x". In cases were both (1) and (2) apply, (1) is
     * used.
     */
    @Override
    public String toString() {
        if(this.terms.size() == 0) {
            return "0";
        } else if(this.isNaN()) {
            return "NaN";
        }

        StringBuilder output = new StringBuilder();
        boolean isFirst = true;
        for(RatTerm rt : this.terms) {
            if(isFirst) {
                isFirst = false;
                output.append(rt.toString());
            } else {
                if(rt.getCoeff().isNegative()) {
                    output.append(rt.toString());
                } else {
                    output.append("+" + rt.toString());
                }
            }
        }
        return output.toString();
    }

    /**
     * Builds a new RatPoly, given a descriptive String.
     *
     * @param polyStr a string of the format described in the @spec.requires clause.
     * @return a RatPoly p such that p.toString() = polyStr.
     * @spec.requires 'polyStr' is an instance of a string with no spaces that expresses a poly in
     * the form defined in the toString() method.
     * <p>Valid inputs include "0", "x-10", and "x^3-2*x^2+5/3*x+3", and "NaN".
     */
    public static RatPoly valueOf(String polyStr) {
        List<RatTerm> parsedTerms = new ArrayList<>();

        // First we decompose the polyStr into its component terms;
        // third arg orders "+" and "-" to be returned as tokens.
        StringTokenizer termStrings = new StringTokenizer(polyStr, "+-", true);

        boolean nextTermIsNegative = false;
        while(termStrings.hasMoreTokens()) {
            String termToken = termStrings.nextToken();

            if(termToken.equals("-")) {
                nextTermIsNegative = true;
            } else if(termToken.equals("+")) {
                nextTermIsNegative = false;
            } else {
                // Not "+" or "-"; must be a term
                RatTerm term = RatTerm.valueOf(termToken);

                // at this point, coeff and expt are initialized.
                // Need to fix coeff if it was proceeded by a '-'
                if(nextTermIsNegative) {
                    term = term.negate();
                }

                // accumulate terms of polynomial in 'parsedTerms'
                sortedInsert(parsedTerms, term);
            }
        }
        return new RatPoly(parsedTerms);
    }

    /**
     * Standard hashCode function.
     *
     * @return an int that all objects equal to this will also return.
     */
    @Override
    public int hashCode() {
        // all instances that are NaN must return the same hashCode
        if(this.isNaN()) {
            return 0;
        }
        return this.terms.hashCode();
    }

    /**
     * Standard equality operation.
     *
     * @param obj the object to be compared for equality
     * @return true if and only if 'obj' is an instance of a RatPoly and 'this' and 'obj' represent
     * the same rational polynomial. Note that all NaN RatPolys are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RatPoly) {
            RatPoly rp = (RatPoly) obj;

            // special case: check if both are NaN
            if(this.isNaN() && rp.isNaN()) {
                return true;
            } else {
                return this.terms.equals(rp.terms);
            }
        } else {
            return false;
        }
    }
}
